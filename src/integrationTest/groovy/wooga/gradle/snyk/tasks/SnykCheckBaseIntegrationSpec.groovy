/*
 * Copyright 2022 Wooga GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package wooga.gradle.snyk.tasks


import com.wooga.gradle.test.queries.PropertyQuery
import com.wooga.gradle.test.queries.TestValue
import com.wooga.gradle.test.writers.CommandLinePropertyEvaluation
import com.wooga.gradle.test.writers.PropertyGetterTaskWriter
import com.wooga.gradle.test.writers.PropertySetInvocation
import com.wooga.gradle.test.writers.PropertySetterWriter
import spock.lang.Unroll
import wooga.gradle.snyk.cli.FailOnOption
import wooga.gradle.snyk.cli.SeverityThresholdOption
import wooga.gradle.snyk.cli.VulnerablePathsOption

abstract class SnykCheckBaseIntegrationSpec<T extends SnykTask> extends SnykTaskIntegrationSpec<T> {

    abstract String getCommandName()

    @Unroll("can set property #property with cli option #cliOption")
    def "can set property via cli option"() {

        given: "disable subject under test to no fail"
        appendToSubjectTask("enabled=false")

        expect:
        runPropertyQuery(subjectUnderTestName, getter, setter).matches(value)

        where:
        property                         | cliOption                          | value                                      | type
        "allProjects"                    | "--all-projects"                   | true                                       | "Boolean"
        "detectionDepth"                 | "--detection-depth"                | 22                                         | "Integer"
        "exclude"                        | "--exclude"                        | [osPath("/path/one"), osPath("/path/two")] | "CLIList"
        "pruneRepeatedSubDependencies"   | "--prune-repeated-subdependencies" | true                                       | "Boolean"
        "printDependencies"              | "--print-deps"                     | true                                       | "Boolean"
        "remoteRepoUrl"                  | "--remote-repo-url"                | "https://some/url"                         | "CLIString"
        "includeDevelopmentDependencies" | "--dev"                            | true                                       | "Boolean"
        "orgName"                        | "--org"                            | "testOrg"                                  | "CLIString"
        "allProjects"                    | "--all-projects"                   | true                                       | "Boolean"
        "ignorePolicy"                   | "--ignore-policy"                  | true                                       | "Boolean"
        "showVulnerablePaths"            | "--show-vulnerable-paths"          | VulnerablePathsOption.all                  | "CLIString"
        "projectName"                    | "--project-name"                   | "some-project"                             | "CLIString"
        "targetReference"                | "--target-reference"               | "some-reference"                           | "CLIString"
        "policyPath"                     | "--policy-path"                    | osPath("/path/to/policy.snyk")             | "CLIString"
        "printJson"                      | "--json"                           | true                                       | "Boolean"
        "jsonOutputPath"                 | "--json-file-output"               | osPath("/path/to/output.json")             | "CLIString"
        "printSarif"                     | "--sarif"                          | true                                       | "Boolean"
        "sarifOutputPath"                | "--sarif-file-output"              | osPath("/path/to/output.sarif")            | "CLIString"
        "severityThreshold"              | "--severity-threshold"             | SeverityThresholdOption.low                | "CLIString"
        "failOn"                         | "--fail-on"                        | FailOnOption.upgradable                    | "CLIString"

        "assetsProjectName"              | "--assets-project-name"            | true                                       | "Boolean"
        "packagesFolder"                 | "--packages-folder"                | osPath("/path/to/packages")                | "CLIString"
        "projectNamePrefix"              | "--project-name-prefix"            | "some_prefix"                              | "CLIString"

        "strictOutOfSync"                | "--strict-out-of-sync"             | true                                       | "Boolean"

        "insecure"                       | "--insecure"                       | true                                       | "Boolean"

        "scanAllUnmanaged"               | "--scan-all-unmanaged"             | true                                       | "Boolean"
        "reachable"                      | "--reachable"                      | true                                       | "Boolean"
        "reachableTimeout"               | "--reachable-timeout"              | 22                                         | "Integer"

        "subProject"                     | "--sub-project"                    | "sub-project-1"                            | "CLIString"
        "allSubProjects"                 | "--all-sub-projects"               | true                                       | "Boolean"
        "configurationMatching"          | "--configuration-matching"         | "someConfig"                               | "CLIString"
        "configurationAttributes"        | "--configuration-attributes"       | ["attr1", "attr2"]                         | "CLIList"
        "initScript"                     | "--gradle-init-script"             | osPath("/path/to/init-script")             | "CLIString"

        "command"                        | "--command"                        | "someCommand"                              | "CLIString"
        "skipUnresolved"                 | "--skip-unresolved"                | true                                       | "Boolean"

        "yarnWorkspaces"                 | "--yarn-workspaces"                | true                                       | "Boolean"

        "packageFile"                    | "--file"                           | osPath("/path/to/package")                 | "CLIString"
        "packageManager"                 | "--package-manager"                | "someValue"                                | "CLIString"

        setter = new PropertySetterWriter(subjectUnderTestName, property)
            .set(value, type)
            .toCommandLine(cliOption)
            .serialize(wrapValueFallback)

        getter = new PropertyGetterTaskWriter(setter)
    }

    @Unroll("can set property #property with #method and type #type")
    def "can set property SnykTask"() {

        given: "disable subject under test to no fail"
        appendToSubjectTask("enabled=false")

        expect:
        runPropertyQuery(subjectUnderTestName, getter, setter).matches(rawValue)

        where:
        property                         | method                                              | type                                | rawValue
        "allProjects"                    | PropertySetInvocation.providerSet                   | "Boolean"                           | true
        "allProjects"                    | PropertySetInvocation.providerSet                   | "Provider<Boolean>"                 | false
        "allProjects"                    | PropertySetInvocation.setter                        | "Boolean"                           | true
        "allProjects"                    | PropertySetInvocation.setter                        | "Provider<Boolean>"                 | false

        "detectionDepth"                 | PropertySetInvocation.providerSet                   | "Integer"                           | 1
        "detectionDepth"                 | PropertySetInvocation.providerSet                   | "Provider<Integer>"                 | 2
        "detectionDepth"                 | PropertySetInvocation.setter                        | "Integer"                           | 3
        "detectionDepth"                 | PropertySetInvocation.setter                        | "Provider<Integer>"                 | 4
        "detectionDepth"                 | PropertySetInvocation.method                        | "String"                            | 4

        "exclude"                        | PropertySetInvocation.providerSet                   | "List<File>"                        | [osPath("/path/to/dir")]
        "exclude"                        | PropertySetInvocation.providerSet                   | "Provider<List<File>>"              | [osPath("/path/to/dir")]
        "exclude"                        | PropertySetInvocation.setter                        | "List<File>"                        | [osPath("/path/to/dir")]
        "exclude"                        | PropertySetInvocation.setter                        | "Provider<List<File>>"              | [osPath("/path/to/dir")]
        "exclude"                        | PropertySetInvocation.method                        | "List<File>"                        | [osPath("/path/to/dir")]
        "exclude"                        | PropertySetInvocation.method                        | "File"                              | TestValue.set(osPath("/path/to/dir")).expectList()
        "exclude"                        | PropertySetInvocation.method                        | "File..."                           | [osPath("/path/to/dir")]
        "exclude"                        | PropertySetInvocation.customSetter("excludeOption") | "String"                            | TestValue.join([osPath('/path/to/dir'), osPath('/path/to/dir2')], ",").expectList()

        "pruneRepeatedSubDependencies"   | PropertySetInvocation.providerSet                   | "Boolean"                           | true
        "pruneRepeatedSubDependencies"   | PropertySetInvocation.providerSet                   | "Provider<Boolean>"                 | false
        "pruneRepeatedSubDependencies"   | PropertySetInvocation.setter                        | "Boolean"                           | true
        "pruneRepeatedSubDependencies"   | PropertySetInvocation.setter                        | "Provider<Boolean>"                 | false

        "printDependencies"              | PropertySetInvocation.providerSet                   | "Boolean"                           | true
        "printDependencies"              | PropertySetInvocation.providerSet                   | "Provider<Boolean>"                 | false
        "printDependencies"              | PropertySetInvocation.setter                        | "Boolean"                           | true
        "printDependencies"              | PropertySetInvocation.setter                        | "Provider<Boolean>"                 | false

        "remoteRepoUrl"                  | PropertySetInvocation.providerSet                   | "String"                            | "http://remote/url/1"
        "remoteRepoUrl"                  | PropertySetInvocation.providerSet                   | "Provider<String>"                  | "http://remote/url/2"
        "remoteRepoUrl"                  | PropertySetInvocation.setter                        | "String"                            | "http://remote/url/3"
        "remoteRepoUrl"                  | PropertySetInvocation.setter                        | "Provider<String>"                  | "http://remote/url/4"

        "includeDevelopmentDependencies" | PropertySetInvocation.providerSet                   | "Boolean"                           | true
        "includeDevelopmentDependencies" | PropertySetInvocation.providerSet                   | "Provider<Boolean>"                 | false
        "includeDevelopmentDependencies" | PropertySetInvocation.setter                        | "Boolean"                           | true
        "includeDevelopmentDependencies" | PropertySetInvocation.setter                        | "Provider<Boolean>"                 | false

        "orgName"                        | PropertySetInvocation.providerSet                   | "String"                            | "org1"
        "orgName"                        | PropertySetInvocation.providerSet                   | "Provider<String>"                  | "org2"
        "orgName"                        | PropertySetInvocation.setter                        | "String"                            | "org3"
        "orgName"                        | PropertySetInvocation.setter                        | "Provider<String>"                  | "org4"

        "packageFile"                    | PropertySetInvocation.providerSet                   | "File"                              | osPath("/path/to/file1")
        "packageFile"                    | PropertySetInvocation.providerSet                   | "Provider<RegularFile>"             | osPath("/path/to/file2")
        "packageFile"                    | PropertySetInvocation.setter                        | "File"                              | osPath("/path/to/file3")
        "packageFile"                    | PropertySetInvocation.setter                        | "Provider<RegularFile>"             | osPath("/path/to/file4")
        "packageFile"                    | PropertySetInvocation.method                        | "String"                            | osPath("/path/to/file5")

        "packageManager"                 | PropertySetInvocation.providerSet                   | "String"                            | "nuget"
        "packageManager"                 | PropertySetInvocation.providerSet                   | "Provider<String>"                  | "paket"
        "packageManager"                 | PropertySetInvocation.setter                        | "String"                            | "npm"
        "packageManager"                 | PropertySetInvocation.setter                        | "Provider<String>"                  | "gradle"

        "ignorePolicy"                   | PropertySetInvocation.providerSet                   | "Boolean"                           | true
        "ignorePolicy"                   | PropertySetInvocation.providerSet                   | "Provider<Boolean>"                 | false
        "ignorePolicy"                   | PropertySetInvocation.setter                        | "Boolean"                           | true
        "ignorePolicy"                   | PropertySetInvocation.setter                        | "Provider<Boolean>"                 | false

        "showVulnerablePaths"            | PropertySetInvocation.providerSet                   | "VulnerablePathsOption"             | VulnerablePathsOption.some
        "showVulnerablePaths"            | PropertySetInvocation.providerSet                   | "Provider<VulnerablePathsOption>"   | VulnerablePathsOption.none
        "showVulnerablePaths"            | PropertySetInvocation.setter                        | "VulnerablePathsOption"             | VulnerablePathsOption.all
        "showVulnerablePaths"            | PropertySetInvocation.setter                        | "Provider<VulnerablePathsOption>"   | VulnerablePathsOption.some

        "projectName"                    | PropertySetInvocation.providerSet                   | "String"                            | "test1"
        "projectName"                    | PropertySetInvocation.providerSet                   | "Provider<String>"                  | "test2"
        "projectName"                    | PropertySetInvocation.setter                        | "String"                            | "test3"
        "projectName"                    | PropertySetInvocation.setter                        | "Provider<String>"                  | "test4"

        "targetReference"                | PropertySetInvocation.providerSet                   | "String"                            | "test_reference_1"
        "targetReference"                | PropertySetInvocation.providerSet                   | "Provider<String>"                  | "test_reference_2"
        "targetReference"                | PropertySetInvocation.setter                        | "String"                            | "test_reference_3"
        "targetReference"                | PropertySetInvocation.setter                        | "Provider<String>"                  | "test_reference_4"

        "policyPath"                     | PropertySetInvocation.providerSet                   | "File"                              | osPath("/path/to/file1")
        "policyPath"                     | PropertySetInvocation.providerSet                   | "Provider<RegularFile>"             | osPath("/path/to/file2")
        "policyPath"                     | PropertySetInvocation.setter                        | "File"                              | osPath("/path/to/file3")
        "policyPath"                     | PropertySetInvocation.setter                        | "Provider<RegularFile>"             | osPath("/path/to/file4")
        "policyPath"                     | PropertySetInvocation.method                        | "String"                            | osPath("/path/to/file5")

        "printJson"                      | PropertySetInvocation.providerSet                   | "Boolean"                           | true
        "printJson"                      | PropertySetInvocation.providerSet                   | "Provider<Boolean>"                 | true
        "printJson"                      | PropertySetInvocation.setter                        | "Boolean"                           | true
        "printJson"                      | PropertySetInvocation.setter                        | "Provider<Boolean>"                 | true

        "jsonOutputPath"                 | PropertySetInvocation.providerSet                   | "File"                              | osPath("/path/to/file1")
        "jsonOutputPath"                 | PropertySetInvocation.providerSet                   | "Provider<RegularFile>"             | osPath("/path/to/file2")
        "jsonOutputPath"                 | PropertySetInvocation.setter                        | "File"                              | osPath("/path/to/file3")
        "jsonOutputPath"                 | PropertySetInvocation.setter                        | "Provider<RegularFile>"             | osPath("/path/to/file4")
        "jsonOutputPath"                 | PropertySetInvocation.method                        | "String"                            | osPath("/path/to/file5")

        "printSarif"                     | PropertySetInvocation.providerSet                   | "Boolean"                           | true
        "printSarif"                     | PropertySetInvocation.providerSet                   | "Provider<Boolean>"                 | true
        "printSarif"                     | PropertySetInvocation.setter                        | "Boolean"                           | true
        "printSarif"                     | PropertySetInvocation.setter                        | "Provider<Boolean>"                 | true

        "sarifOutputPath"                | PropertySetInvocation.providerSet                   | "File"                              | osPath("/path/to/file1")
        "sarifOutputPath"                | PropertySetInvocation.providerSet                   | "Provider<RegularFile>"             | osPath("/path/to/file2")
        "sarifOutputPath"                | PropertySetInvocation.setter                        | "File"                              | osPath("/path/to/file3")
        "sarifOutputPath"                | PropertySetInvocation.setter                        | "Provider<RegularFile>"             | osPath("/path/to/file4")
        "sarifOutputPath"                | PropertySetInvocation.method                        | "String"                            | osPath("/path/to/file5")

        "severityThreshold"              | PropertySetInvocation.providerSet                   | "SeverityThresholdOption"           | SeverityThresholdOption.low
        "severityThreshold"              | PropertySetInvocation.providerSet                   | "Provider<SeverityThresholdOption>" | SeverityThresholdOption.medium
        "severityThreshold"              | PropertySetInvocation.setter                        | "SeverityThresholdOption"           | SeverityThresholdOption.high
        "severityThreshold"              | PropertySetInvocation.setter                        | "Provider<SeverityThresholdOption>" | SeverityThresholdOption.critical

        "failOn"                         | PropertySetInvocation.providerSet                   | "FailOnOption"                      | FailOnOption.all
        "failOn"                         | PropertySetInvocation.providerSet                   | "Provider<FailOnOption>"            | FailOnOption.upgradable
        "failOn"                         | PropertySetInvocation.setter                        | "FailOnOption"                      | FailOnOption.patchable
        "failOn"                         | PropertySetInvocation.setter                        | "Provider<FailOnOption>"            | FailOnOption.all

        "compilerArguments"              | PropertySetInvocation.providerSet                   | "List<String>"                      | ["--foo", "--bar"]
        "compilerArguments"              | PropertySetInvocation.providerSet                   | "Provider<List<String>>"            | ["--foo", "--bar"]
        "compilerArguments"              | PropertySetInvocation.setter                        | "List<String>"                      | ["--foo", "--bar"]
        "compilerArguments"              | PropertySetInvocation.setter                        | "Provider<List<String>>"            | ["--foo", "--bar"]
        "compilerArguments"              | PropertySetInvocation.method                        | "List<String>"                      | ["--foo", "--bar"]
        "compilerArguments"              | PropertySetInvocation.method                        | "String"                            | TestValue.set("--foo").expectList()
        "compilerArguments"              | PropertySetInvocation.method                        | "String..."                         | ["--foo", "--bar"]

        "assetsProjectName"              | PropertySetInvocation.providerSet                   | "Boolean"                           | true
        "assetsProjectName"              | PropertySetInvocation.providerSet                   | "Provider<Boolean>"                 | true
        "assetsProjectName"              | PropertySetInvocation.setter                        | "Boolean"                           | true
        "assetsProjectName"              | PropertySetInvocation.setter                        | "Provider<Boolean>"                 | true

        "packagesFolder"                 | PropertySetInvocation.providerSet                   | "File"                              | osPath("/path/to/packages1")
        "packagesFolder"                 | PropertySetInvocation.providerSet                   | "Provider<Directory>"               | osPath("/path/to/packages2")
        "packagesFolder"                 | PropertySetInvocation.setter                        | "File"                              | osPath("/path/to/packages3")
        "packagesFolder"                 | PropertySetInvocation.setter                        | "Provider<Directory>"               | osPath("/path/to/packages4")
        "packagesFolder"                 | PropertySetInvocation.method                        | "String"                            | osPath("/path/to/packages5")

        "projectNamePrefix"              | PropertySetInvocation.providerSet                   | "String"                            | "prefix_1"
        "projectNamePrefix"              | PropertySetInvocation.providerSet                   | "Provider<String>"                  | "prefix_2"
        "projectNamePrefix"              | PropertySetInvocation.setter                        | "String"                            | "prefix_3"
        "projectNamePrefix"              | PropertySetInvocation.setter                        | "Provider<String>"                  | "prefix_4"

        "strictOutOfSync"                | PropertySetInvocation.providerSet                   | "Boolean"                           | true
        "strictOutOfSync"                | PropertySetInvocation.providerSet                   | "Provider<Boolean>"                 | true
        "strictOutOfSync"                | PropertySetInvocation.setter                        | "Boolean"                           | true
        "strictOutOfSync"                | PropertySetInvocation.setter                        | "Provider<Boolean>"                 | true

        "insecure"                       | PropertySetInvocation.providerSet                   | "Boolean"                           | true
        "insecure"                       | PropertySetInvocation.providerSet                   | "Provider<Boolean>"                 | true
        "insecure"                       | PropertySetInvocation.setter                        | "Boolean"                           | true
        "insecure"                       | PropertySetInvocation.setter                        | "Provider<Boolean>"                 | true

        "scanAllUnmanaged"               | PropertySetInvocation.providerSet                   | "Boolean"                           | true
        "scanAllUnmanaged"               | PropertySetInvocation.providerSet                   | "Provider<Boolean>"                 | true
        "scanAllUnmanaged"               | PropertySetInvocation.setter                        | "Boolean"                           | true
        "scanAllUnmanaged"               | PropertySetInvocation.setter                        | "Provider<Boolean>"                 | true

        "reachable"                      | PropertySetInvocation.providerSet                   | "Boolean"                           | true
        "reachable"                      | PropertySetInvocation.providerSet                   | "Provider<Boolean>"                 | true
        "reachable"                      | PropertySetInvocation.setter                        | "Boolean"                           | true
        "reachable"                      | PropertySetInvocation.setter                        | "Provider<Boolean>"                 | true

        "reachableTimeout"               | PropertySetInvocation.providerSet                   | "Integer"                           | 1
        "reachableTimeout"               | PropertySetInvocation.providerSet                   | "Provider<Integer>"                 | 2
        "reachableTimeout"               | PropertySetInvocation.setter                        | "Integer"                           | 3
        "reachableTimeout"               | PropertySetInvocation.setter                        | "Provider<Integer>"                 | 4
        "reachableTimeout"               | PropertySetInvocation.method                        | "String"                            | 4

        "subProject"                     | PropertySetInvocation.providerSet                   | "String"                            | "subproject_1"
        "subProject"                     | PropertySetInvocation.providerSet                   | "Provider<String>"                  | "subproject_2"
        "subProject"                     | PropertySetInvocation.setter                        | "String"                            | "subproject_3"
        "subProject"                     | PropertySetInvocation.setter                        | "Provider<String>"                  | "subproject_4"

        "allSubProjects"                 | PropertySetInvocation.providerSet                   | "Boolean"                           | true
        "allSubProjects"                 | PropertySetInvocation.providerSet                   | "Provider<Boolean>"                 | true
        "allSubProjects"                 | PropertySetInvocation.setter                        | "Boolean"                           | true
        "allSubProjects"                 | PropertySetInvocation.setter                        | "Provider<Boolean>"                 | true

        "configurationMatching"          | PropertySetInvocation.providerSet                   | "String"                            | "subproject_1"
        "configurationMatching"          | PropertySetInvocation.providerSet                   | "Provider<String>"                  | "subproject_2"
        "configurationMatching"          | PropertySetInvocation.setter                        | "String"                            | "subproject_3"
        "configurationMatching"          | PropertySetInvocation.setter                        | "Provider<String>"                  | "subproject_4"

        "configurationAttributes"        | PropertySetInvocation.providerSet                   | "List<String>"                      | ["config1", "config2"]
        "configurationAttributes"        | PropertySetInvocation.providerSet                   | "Provider<List<String>>"            | ["config1", "config2"]
        "configurationAttributes"        | PropertySetInvocation.setter                        | "List<String>"                      | ["config1", "config2"]
        "configurationAttributes"        | PropertySetInvocation.setter                        | "Provider<List<String>>"            | ["config1", "config2"]
        "configurationAttributes"        | PropertySetInvocation.method                        | "List<String>"                      | ["config1", "config2"]
        "configurationAttributes"        | PropertySetInvocation.method                        | "String"                            | TestValue.set("config1").expectList()
        "configurationAttributes"        | PropertySetInvocation.method                        | "String..."                         | ["config1", "config2"]

        "command"                        | PropertySetInvocation.providerSet                   | "String"                            | "command_1"
        "command"                        | PropertySetInvocation.providerSet                   | "Provider<String>"                  | "command_2"
        "command"                        | PropertySetInvocation.setter                        | "String"                            | "command_3"
        "command"                        | PropertySetInvocation.setter                        | "Provider<String>"                  | "command_4"

        "initScript"                     | PropertySetInvocation.providerSet                   | "File"                              | osPath("/path/to/initScript1")
        "initScript"                     | PropertySetInvocation.providerSet                   | "Provider<RegularFile>"             | osPath("/path/to/initScript2")
        "initScript"                     | PropertySetInvocation.setter                        | "File"                              | osPath("/path/to/initScript3")
        "initScript"                     | PropertySetInvocation.setter                        | "Provider<RegularFile>"             | osPath("/path/to/initScript4")
        "initScript"                     | PropertySetInvocation.method                        | "String"                            | osPath("/path/to/initScript5")

        "skipUnresolved"                 | PropertySetInvocation.providerSet                   | "Boolean"                           | true
        "skipUnresolved"                 | PropertySetInvocation.providerSet                   | "Provider<Boolean>"                 | true
        "skipUnresolved"                 | PropertySetInvocation.setter                        | "Boolean"                           | true
        "skipUnresolved"                 | PropertySetInvocation.setter                        | "Provider<Boolean>"                 | true

        "yarnWorkspaces"                 | PropertySetInvocation.providerSet                   | "Boolean"                           | true
        "yarnWorkspaces"                 | PropertySetInvocation.providerSet                   | "Provider<Boolean>"                 | true
        "yarnWorkspaces"                 | PropertySetInvocation.setter                        | "Boolean"                           | true
        "yarnWorkspaces"                 | PropertySetInvocation.setter                        | "Provider<Boolean>"                 | true

        setter = new PropertySetterWriter(subjectUnderTestName, property)
            .set(rawValue, type)
            .toScript(method)
            .serialize(wrapValueFallback)

        getter = new PropertyGetterTaskWriter(setter)
    }

    @Unroll
    def "help prints commandline flag '#commandlineFlag' description"() {
        when:
        def result = runTasksSuccessfully("help", "--task", subjectUnderTestName)

        then:
        result.standardOutput.contains("Description")
        result.standardOutput.contains("Group")

        result.standardOutput.contains(commandlineFlag)

        where:
        commandlineFlag                    | _
        "--all-projects"                   | _
        "--all-sub-projects"               | _
        "--assets-project-name"            | _
        "--command"                        | _
        "--configuration-attributes"       | _
        "--configuration-matching"         | _
        "--detection-depth"                | _
        "--dev"                            | _
        "--exclude"                        | _
        "--fail-on"                        | _
        "--file"                           | _
        "--package-manager"                | _
        "--ignore-policy"                  | _
        "--gradle-init-script"             | _
        "--json"                           | _
        "--json-file-output"               | _
        "--org"                            | _
        "--packages-folder"                | _
        "--policy-path"                    | _
        "--print-deps"                     | _
        "--project-name-prefix"            | _
        "--project-name"                   | _
        "--prune-repeated-subdependencies" | _
        "--reachable"                      | _
        "--reachable-timeout"              | _
        "--remote-repo-url"                | _
        "--sarif"                          | _
        "--sarif-file-output"              | _
        "--scan-all-unmanaged"             | _
        "--severity-threshold"             | _
        "--show-vulnerable-paths"          | _
        "--skip-unresolved"                | _
        "--strict-out-of-sync"             | _
        "--sub-project"                    | _
        "--target-reference"               | _
        "--yarn-workspaces"                | _
    }

    @Unroll()
    def "composes correct CLI option #flag from property #property with value #value"() {

        given: "a snyk wrapper"
        setSnykWrapper(true, subjectUnderTestName)

        when:
        def result = runTasksSuccessfully(subjectUnderTestName, setter)
        def query = new PropertyQuery(pattern, this, result.standardOutput, new CommandLinePropertyEvaluation())
        query.withFilePathsRelativeToProject()

        then:
        query.matches(value, type)

        where:
        property                         | type           | value                                           | flag
        // TestOption
        "allProjects"                    | Boolean        | true                                            | "--all-projects"
        "projectName"                    | String         | "pancakes"                                      | "--project-name"
        "detectionDepth"                 | Integer        | 7                                               | "--detection-depth"
        "exclude"                        | "List<File>"   | TestValue.projectFile("foo.bar")                | "--exclude"
        "pruneRepeatedSubDependencies"   | Boolean        | true                                            | "--prune-repeated-subdependencies"
        "printDependencies"              | Boolean        | true                                            | "--print-deps"
        "remoteRepoUrl"                  | String         | "foo.bar/pancakes"                              | "--remote-repo-url"
        "includeDevelopmentDependencies" | Boolean        | true                                            | "--dev"
        "orgName"                        | String         | "PANCAKES"                                      | "--org"
        "packageFile"                    | File           | TestValue.set("foo.bar").expect("foo.bar")      | "--file"
        "packageManager"                 | String         | "nuget"                                         | "--package-manager"
        "ignorePolicy"                   | Boolean        | true                                            | "--ignore-policy"
        "showVulnerablePaths"            | String         | "all"                                           | "--show-vulnerable-paths"
        "targetReference"                | String         | "foobar"                                        | "--target-reference"
        "policyPath"                     | File           | "foo.bar"                                       | "--policy-path"
        "printJson"                      | Boolean        | true                                            | "--json"
        "jsonOutputPath"                 | File           | "foo.bar"                                       | "--json-file-output"
        "printSarif"                     | Boolean        | true                                            | "--sarif"
        "sarifOutputPath"                | File           | "foo.bar"                                       | "--sarif-file-output"
        "severityThreshold"              | String         | "critical"                                      | "--severity-threshold"
        "failOn"                         | FailOnOption   | FailOnOption.all                                | "--fail-on"
        // ProjectOption
        "scanAllUnmanaged"               | Boolean        | true                                            | "--scan-all-unmanaged"
        "subProject"                     | String         | "foobar"                                        | "--sub-project"
        "allSubProjects"                 | Boolean        | true                                            | "--all-sub-projects"
        "configurationMatching"          | String         | "foobar"                                        | "--configuration-matching"
        "configurationAttributes"        | "List<String>" | TestValue.set(["foo", "bar"]).expect("foo,bar") | "--configuration-attributes"
        "initScript"                     | File           | "foo.bar"                                       | "--init-script"
        "reachable"                      | Boolean        | true                                            | "--reachable"
        "reachableTimeout"               | Integer        | 7                                               | "--reachable-timeout"
        "assetsProjectName"              | Boolean        | true                                            | "--assets-project-name"
        "projectNamePrefix"              | String         | "foobar"                                        | "--project-name-prefix"
        "strictOutOfSync"                | Boolean        | true                                            | "--strict-out-of-sync"
        "yarnWorkspaces"                 | Boolean        | true                                            | "--yarn-workspaces"
        "skipUnresolved"                 | Boolean        | true                                            | "--skip-unresolved"
        "command"                        | String         | "foobar"                                        | "--command"
        "debug"                          | Boolean        | true                                            | "-d"

        setter = new PropertySetterWriter(subjectUnderTestName, property)
            .set(value, type)
            .serialize(wrapValueFallback)
            .toScript(PropertySetInvocation.assignment)
            .withFilesRelativeToProjectDirectory(true)

        pattern = flag.toString().empty ? commandName : "${commandName} ${flag}"
    }

    @Override
    String wrapValueBasedOnType(Object rawValue, Class type) {
        if (type.isEnum() && rawValue != null) {
            return "${type.name}.${rawValue}"
        }
        return super.wrapValueBasedOnType(rawValue, type)
    }

    String wrap(String value) {
        wrapValueBasedOnType(value, String)
    }
}
