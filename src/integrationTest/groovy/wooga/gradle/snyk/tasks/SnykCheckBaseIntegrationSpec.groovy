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


import com.wooga.gradle.test.PropertyQueryTaskWriter
import spock.lang.Unroll
import wooga.gradle.snyk.cli.FailOnOption
import wooga.gradle.snyk.cli.SeverityThresholdOption
import wooga.gradle.snyk.cli.VulnerablePathsOption

import static com.wooga.gradle.PlatformUtils.escapedPath
import static com.wooga.gradle.test.PropertyUtils.toProviderSet
import static com.wooga.gradle.test.PropertyUtils.toSetter

abstract class SnykCheckBaseIntegrationSpec<T extends SnykTask> extends SnykTaskIntegrationSpec<T> {

    abstract String getCommandName()

    @Unroll("can set property #property with cli option #cliOption")
    def "can set property via cli option"() {
        given: "a task to read back the value"
        def query = new PropertyQueryTaskWriter("${subjectUnderTestName}.${property}")
        query.write(buildFile)

        and: "tasks to execute"
        def tasks = [subjectUnderTestName, cliOption]
        if (rawValue != _) {
            tasks.add(value)
        }
        tasks.add(query.taskName)

        and: "disable subject under test to no fail"
        appendToSubjectTask("enabled=false")

        when:
        def result = runTasksSuccessfully(*tasks)

        then:
        query.matches(result, expectedValue)

        where:
        property                         | cliOption                          | rawValue                                   | returnValue | type
        "allProjects"                    | "--all-projects"                   | _                                          | true        | "Boolean"
        "detectionDepth"                 | "--detection-depth"                | 22                                         | _           | "Integer"
        "exclude"                        | "--exclude"                        | [osPath("/path/one"), osPath("/path/two")] | _           | "CLIList"
        "pruneRepeatedSubDependencies"   | "--prune-repeated-subdependencies" | _                                          | true        | "Boolean"
        "printDependencies"              | "--print-deps"                     | _                                          | true        | "Boolean"
        "remoteRepoUrl"                  | "--remote-repo-url"                | "https://some/url"                         | _           | "CLIString"
        "includeDevelopmentDependencies" | "--dev"                            | _                                          | true        | "Boolean"
        "orgName"                        | "--org"                            | "testOrg"                                  | _           | "CLIString"
        "allProjects"                    | "--all-projects"                   | _                                          | true        | "Boolean"
        "ignorePolicy"                   | "--ignore-policy"                  | _                                          | true        | "Boolean"
        "showVulnerablePaths"            | "--show-vulnerable-paths"          | VulnerablePathsOption.all                  | _           | "CLIString"
        "projectName"                    | "--project-name"                   | "some-project"                             | _           | "CLIString"
        "targetReference"                | "--target-reference"               | "some-reference"                           | _           | "CLIString"
        "policyPath"                     | "--policy-path"                    | osPath("/path/to/policy.snyk")             | _           | "CLIString"
        "printJson"                      | "--json"                           | _                                          | true        | "Boolean"
        "jsonOutputPath"                 | "--json-file-output"               | osPath("/path/to/output.json")             | _           | "CLIString"
        "printSarif"                     | "--sarif"                          | _                                          | true        | "Boolean"
        "sarifOutputPath"                | "--sarif-file-output"              | osPath("/path/to/output.sarif")            | _           | "CLIString"
        "severityThreshold"              | "--severity-threshold"             | SeverityThresholdOption.low                | _           | "CLIString"
        "failOn"                         | "--fail-on"                        | FailOnOption.upgradable                    | _           | "CLIString"

        "assetsProjectName"              | "--assets-project-name"            | _                                          | true        | "Boolean"
        "packagesFolder"                 | "--packages-folder"                | osPath("/path/to/packages")                | _           | "CLIString"
        "projectNamePrefix"              | "--project-name-prefix"            | "some_prefix"                              | _           | "CLIString"

        "strictOutOfSync"                | "--strict-out-of-sync"             | _                                          | true        | "Boolean"

        "insecure"                       | "--insecure"                       | _                                          | true        | "Boolean"

        "scanAllUnmanaged"               | "--scan-all-unmanaged"             | _                                          | true        | "Boolean"
        "reachable"                      | "--reachable"                      | _                                          | true        | "Boolean"
        "reachableTimeout"               | "--reachable-timeout"              | 22                                         | _           | "Integer"

        "subProject"                     | "--sub-project"                    | "sub-project-1"                            | _           | "CLIString"
        "allSubProjects"                 | "--all-sub-projects"               | _                                          | true        | "Boolean"
        "configurationMatching"          | "--configuration-matching"         | "someConfig"                               | _           | "CLIString"
        "configurationAttributes"        | "--configuration-attributes"       | ["attr1", "attr2"]                         | _           | "CLIList"
        "initScript"                     | "--gradle-init-script"             | osPath("/path/to/init-script")             | _           | "CLIString"

        "command"                        | "--command"                        | "someCommand"                              | _           | "CLIString"
        "skipUnresolved"                 | "--skip-unresolved"                | _                                          | true        | "Boolean"

        "yarnWorkspaces"                 | "--yarn-workspaces"                | _                                          | true        | "Boolean"

        value = wrapValueBasedOnType(rawValue, type, wrapValueFallback)
        expectedValue = returnValue == _ ? rawValue : returnValue
    }

    @Unroll("can set property #property with #method and type #type")
    def "can set property SnykTask"() {
        given: "a task to read back the value"
        def query = new PropertyQueryTaskWriter("${subjectUnderTestName}.${property}")
        query.write(buildFile)

        and: "a set property"
        appendToSubjectTask("${method}($value)")

        when:
        def result = runTasksSuccessfully(query.taskName)

        then:
        query.matches(result, expectedValue)

        where:
        property                         | method                  | rawValue                                                    | returnValue                                       | type
        "allProjects"                    | toProviderSet(property) | true                                                        | _                                                 | "Boolean"
        "allProjects"                    | toProviderSet(property) | false                                                       | _                                                 | "Provider<Boolean>"
        "allProjects"                    | toSetter(property)      | true                                                        | _                                                 | "Boolean"
        "allProjects"                    | toSetter(property)      | false                                                       | _                                                 | "Provider<Boolean>"

        "detectionDepth"                 | toProviderSet(property) | 1                                                           | _                                                 | "Integer"
        "detectionDepth"                 | toProviderSet(property) | 2                                                           | _                                                 | "Provider<Integer>"
        "detectionDepth"                 | toSetter(property)      | 3                                                           | _                                                 | "Integer"
        "detectionDepth"                 | toSetter(property)      | 4                                                           | _                                                 | "Provider<Integer>"
        "detectionDepth"                 | property                | 4                                                           | _                                                 | "String"

        "exclude"                        | toProviderSet(property) | [osPath("/path/to/dir")]                                    | _                                                 | "List<File>"
        "exclude"                        | toProviderSet(property) | [osPath("/path/to/dir")]                                    | _                                                 | "Provider<List<File>>"
        "exclude"                        | toSetter(property)      | [osPath("/path/to/dir")]                                    | _                                                 | "List<File>"
        "exclude"                        | toSetter(property)      | [osPath("/path/to/dir")]                                    | _                                                 | "Provider<List<File>>"
        "exclude"                        | property                | [osPath("/path/to/dir")]                                    | _                                                 | "List<File>"
        "exclude"                        | property                | osPath("/path/to/dir")                                      | [osPath('/path/to/dir')]                          | "File"
        "exclude"                        | property                | [osPath("/path/to/dir")]                                    | _                                                 | "File..."
        "exclude"                        | "excludeOption"         | [osPath('/path/to/dir'), osPath('/path/to/dir2')].join(',') | [osPath('/path/to/dir'), osPath('/path/to/dir2')] | "String"

        "pruneRepeatedSubDependencies"   | toProviderSet(property) | true                                                        | _                                                 | "Boolean"
        "pruneRepeatedSubDependencies"   | toProviderSet(property) | false                                                       | _                                                 | "Provider<Boolean>"
        "pruneRepeatedSubDependencies"   | toSetter(property)      | true                                                        | _                                                 | "Boolean"
        "pruneRepeatedSubDependencies"   | toSetter(property)      | false                                                       | _                                                 | "Provider<Boolean>"

        "printDependencies"              | toProviderSet(property) | true                                                        | _                                                 | "Boolean"
        "printDependencies"              | toProviderSet(property) | false                                                       | _                                                 | "Provider<Boolean>"
        "printDependencies"              | toSetter(property)      | true                                                        | _                                                 | "Boolean"
        "printDependencies"              | toSetter(property)      | false                                                       | _                                                 | "Provider<Boolean>"

        "remoteRepoUrl"                  | toProviderSet(property) | "http://remote/url/1"                                       | _                                                 | "String"
        "remoteRepoUrl"                  | toProviderSet(property) | "http://remote/url/2"                                       | _                                                 | "Provider<String>"
        "remoteRepoUrl"                  | toSetter(property)      | "http://remote/url/3"                                       | _                                                 | "String"
        "remoteRepoUrl"                  | toSetter(property)      | "http://remote/url/4"                                       | _                                                 | "Provider<String>"

        "includeDevelopmentDependencies" | toProviderSet(property) | true                                                        | _                                                 | "Boolean"
        "includeDevelopmentDependencies" | toProviderSet(property) | false                                                       | _                                                 | "Provider<Boolean>"
        "includeDevelopmentDependencies" | toSetter(property)      | true                                                        | _                                                 | "Boolean"
        "includeDevelopmentDependencies" | toSetter(property)      | false                                                       | _                                                 | "Provider<Boolean>"

        "orgName"                        | toProviderSet(property) | "org1"                                                      | _                                                 | "String"
        "orgName"                        | toProviderSet(property) | "org2"                                                      | _                                                 | "Provider<String>"
        "orgName"                        | toSetter(property)      | "org3"                                                      | _                                                 | "String"
        "orgName"                        | toSetter(property)      | "org4"                                                      | _                                                 | "Provider<String>"

        "packageFile"                    | toProviderSet(property) | osPath("/path/to/file1")                                    | _                                                 | "File"
        "packageFile"                    | toProviderSet(property) | osPath("/path/to/file2")                                    | _                                                 | "Provider<RegularFile>"
        "packageFile"                    | toSetter(property)      | osPath("/path/to/file3")                                    | _                                                 | "File"
        "packageFile"                    | toSetter(property)      | osPath("/path/to/file4")                                    | _                                                 | "Provider<RegularFile>"
        "packageFile"                    | property                | osPath("/path/to/file5")                                    | _                                                 | "String"

        "ignorePolicy"                   | toProviderSet(property) | true                                                        | _                                                 | "Boolean"
        "ignorePolicy"                   | toProviderSet(property) | false                                                       | _                                                 | "Provider<Boolean>"
        "ignorePolicy"                   | toSetter(property)      | true                                                        | _                                                 | "Boolean"
        "ignorePolicy"                   | toSetter(property)      | false                                                       | _                                                 | "Provider<Boolean>"

        "showVulnerablePaths"            | toProviderSet(property) | VulnerablePathsOption.some                                  | _                                                 | "VulnerablePathsOption"
        "showVulnerablePaths"            | toProviderSet(property) | VulnerablePathsOption.none                                  | _                                                 | "Provider<VulnerablePathsOption>"
        "showVulnerablePaths"            | toSetter(property)      | VulnerablePathsOption.all                                   | _                                                 | "VulnerablePathsOption"
        "showVulnerablePaths"            | toSetter(property)      | VulnerablePathsOption.some                                  | _                                                 | "Provider<VulnerablePathsOption>"

        "projectName"                    | toProviderSet(property) | "test1"                                                     | _                                                 | "String"
        "projectName"                    | toProviderSet(property) | "test2"                                                     | _                                                 | "Provider<String>"
        "projectName"                    | toSetter(property)      | "test3"                                                     | _                                                 | "String"
        "projectName"                    | toSetter(property)      | "test4"                                                     | _                                                 | "Provider<String>"

        "targetReference"                | toProviderSet(property) | "test_reference_1"                                          | _                                                 | "String"
        "targetReference"                | toProviderSet(property) | "test_reference_2"                                          | _                                                 | "Provider<String>"
        "targetReference"                | toSetter(property)      | "test_reference_3"                                          | _                                                 | "String"
        "targetReference"                | toSetter(property)      | "test_reference_4"                                          | _                                                 | "Provider<String>"

        "policyPath"                     | toProviderSet(property) | osPath("/path/to/file1")                                    | _                                                 | "File"
        "policyPath"                     | toProviderSet(property) | osPath("/path/to/file2")                                    | _                                                 | "Provider<RegularFile>"
        "policyPath"                     | toSetter(property)      | osPath("/path/to/file3")                                    | _                                                 | "File"
        "policyPath"                     | toSetter(property)      | osPath("/path/to/file4")                                    | _                                                 | "Provider<RegularFile>"
        "policyPath"                     | property                | osPath("/path/to/file5")                                    | _                                                 | "String"

        "printJson"                      | toProviderSet(property) | true                                                        | _                                                 | "Boolean"
        "printJson"                      | toProviderSet(property) | true                                                        | _                                                 | "Provider<Boolean>"
        "printJson"                      | toSetter(property)      | true                                                        | _                                                 | "Boolean"
        "printJson"                      | toSetter(property)      | true                                                        | _                                                 | "Provider<Boolean>"

        "jsonOutputPath"                 | toProviderSet(property) | osPath("/path/to/file1")                                    | _                                                 | "File"
        "jsonOutputPath"                 | toProviderSet(property) | osPath("/path/to/file2")                                    | _                                                 | "Provider<RegularFile>"
        "jsonOutputPath"                 | toSetter(property)      | osPath("/path/to/file3")                                    | _                                                 | "File"
        "jsonOutputPath"                 | toSetter(property)      | osPath("/path/to/file4")                                    | _                                                 | "Provider<RegularFile>"
        "jsonOutputPath"                 | property                | osPath("/path/to/file5")                                    | _                                                 | "String"

        "printSarif"                     | toProviderSet(property) | true                                                        | _                                                 | "Boolean"
        "printSarif"                     | toProviderSet(property) | true                                                        | _                                                 | "Provider<Boolean>"
        "printSarif"                     | toSetter(property)      | true                                                        | _                                                 | "Boolean"
        "printSarif"                     | toSetter(property)      | true                                                        | _                                                 | "Provider<Boolean>"

        "sarifOutputPath"                | toProviderSet(property) | osPath("/path/to/file1")                                    | _                                                 | "File"
        "sarifOutputPath"                | toProviderSet(property) | osPath("/path/to/file2")                                    | _                                                 | "Provider<RegularFile>"
        "sarifOutputPath"                | toSetter(property)      | osPath("/path/to/file3")                                    | _                                                 | "File"
        "sarifOutputPath"                | toSetter(property)      | osPath("/path/to/file4")                                    | _                                                 | "Provider<RegularFile>"
        "sarifOutputPath"                | property                | osPath("/path/to/file5")                                    | _                                                 | "String"

        "severityThreshold"              | toProviderSet(property) | SeverityThresholdOption.low                                 | _                                                 | "SeverityThresholdOption"
        "severityThreshold"              | toProviderSet(property) | SeverityThresholdOption.medium                              | _                                                 | "Provider<SeverityThresholdOption>"
        "severityThreshold"              | toSetter(property)      | SeverityThresholdOption.high                                | _                                                 | "SeverityThresholdOption"
        "severityThreshold"              | toSetter(property)      | SeverityThresholdOption.critical                            | _                                                 | "Provider<SeverityThresholdOption>"

        "failOn"                         | toProviderSet(property) | FailOnOption.all                                            | _                                                 | "FailOnOption"
        "failOn"                         | toProviderSet(property) | FailOnOption.upgradable                                     | _                                                 | "Provider<FailOnOption>"
        "failOn"                         | toSetter(property)      | FailOnOption.patchable                                      | _                                                 | "FailOnOption"
        "failOn"                         | toSetter(property)      | FailOnOption.all                                            | _                                                 | "Provider<FailOnOption>"

        "compilerArguments"              | toProviderSet(property) | ["--foo", "--bar"]                                          | _                                                 | "List<String>"
        "compilerArguments"              | toProviderSet(property) | ["--foo", "--bar"]                                          | _                                                 | "Provider<List<String>>"
        "compilerArguments"              | toSetter(property)      | ["--foo", "--bar"]                                          | _                                                 | "List<String>"
        "compilerArguments"              | toSetter(property)      | ["--foo", "--bar"]                                          | _                                                 | "Provider<List<String>>"
        "compilerArguments"              | property                | ["--foo", "--bar"]                                          | _                                                 | "List<String>"
        "compilerArguments"              | property                | "--foo"                                                     | ["--foo"]                                         | "String"
        "compilerArguments"              | property                | ["--foo", "--bar"]                                          | _                                                 | "String..."

        "assetsProjectName"              | toProviderSet(property) | true                                                        | _                                                 | "Boolean"
        "assetsProjectName"              | toProviderSet(property) | true                                                        | _                                                 | "Provider<Boolean>"
        "assetsProjectName"              | toSetter(property)      | true                                                        | _                                                 | "Boolean"
        "assetsProjectName"              | toSetter(property)      | true                                                        | _                                                 | "Provider<Boolean>"

        "packagesFolder"                 | toProviderSet(property) | osPath("/path/to/packages1")                                | _                                                 | "File"
        "packagesFolder"                 | toProviderSet(property) | osPath("/path/to/packages2")                                | _                                                 | "Provider<Directory>"
        "packagesFolder"                 | toSetter(property)      | osPath("/path/to/packages3")                                | _                                                 | "File"
        "packagesFolder"                 | toSetter(property)      | osPath("/path/to/packages4")                                | _                                                 | "Provider<Directory>"
        "packagesFolder"                 | property                | osPath("/path/to/packages5")                                | _                                                 | "String"

        "projectNamePrefix"              | toProviderSet(property) | "prefix_1"                                                  | _                                                 | "String"
        "projectNamePrefix"              | toProviderSet(property) | "prefix_2"                                                  | _                                                 | "Provider<String>"
        "projectNamePrefix"              | toSetter(property)      | "prefix_3"                                                  | _                                                 | "String"
        "projectNamePrefix"              | toSetter(property)      | "prefix_4"                                                  | _                                                 | "Provider<String>"

        "strictOutOfSync"                | toProviderSet(property) | true                                                        | _                                                 | "Boolean"
        "strictOutOfSync"                | toProviderSet(property) | true                                                        | _                                                 | "Provider<Boolean>"
        "strictOutOfSync"                | toSetter(property)      | true                                                        | _                                                 | "Boolean"
        "strictOutOfSync"                | toSetter(property)      | true                                                        | _                                                 | "Provider<Boolean>"

        "insecure"                       | toProviderSet(property) | true                                                        | _                                                 | "Boolean"
        "insecure"                       | toProviderSet(property) | true                                                        | _                                                 | "Provider<Boolean>"
        "insecure"                       | toSetter(property)      | true                                                        | _                                                 | "Boolean"
        "insecure"                       | toSetter(property)      | true                                                        | _                                                 | "Provider<Boolean>"

        "scanAllUnmanaged"               | toProviderSet(property) | true                                                        | _                                                 | "Boolean"
        "scanAllUnmanaged"               | toProviderSet(property) | true                                                        | _                                                 | "Provider<Boolean>"
        "scanAllUnmanaged"               | toSetter(property)      | true                                                        | _                                                 | "Boolean"
        "scanAllUnmanaged"               | toSetter(property)      | true                                                        | _                                                 | "Provider<Boolean>"

        "reachable"                      | toProviderSet(property) | true                                                        | _                                                 | "Boolean"
        "reachable"                      | toProviderSet(property) | true                                                        | _                                                 | "Provider<Boolean>"
        "reachable"                      | toSetter(property)      | true                                                        | _                                                 | "Boolean"
        "reachable"                      | toSetter(property)      | true                                                        | _                                                 | "Provider<Boolean>"

        "reachableTimeout"               | toProviderSet(property) | 1                                                           | _                                                 | "Integer"
        "reachableTimeout"               | toProviderSet(property) | 2                                                           | _                                                 | "Provider<Integer>"
        "reachableTimeout"               | toSetter(property)      | 3                                                           | _                                                 | "Integer"
        "reachableTimeout"               | toSetter(property)      | 4                                                           | _                                                 | "Provider<Integer>"
        "reachableTimeout"               | property                | 4                                                           | _                                                 | "String"

        "subProject"                     | toProviderSet(property) | "subproject_1"                                              | _                                                 | "String"
        "subProject"                     | toProviderSet(property) | "subproject_2"                                              | _                                                 | "Provider<String>"
        "subProject"                     | toSetter(property)      | "subproject_3"                                              | _                                                 | "String"
        "subProject"                     | toSetter(property)      | "subproject_4"                                              | _                                                 | "Provider<String>"

        "allSubProjects"                 | toProviderSet(property) | true                                                        | _                                                 | "Boolean"
        "allSubProjects"                 | toProviderSet(property) | true                                                        | _                                                 | "Provider<Boolean>"
        "allSubProjects"                 | toSetter(property)      | true                                                        | _                                                 | "Boolean"
        "allSubProjects"                 | toSetter(property)      | true                                                        | _                                                 | "Provider<Boolean>"

        "configurationMatching"          | toProviderSet(property) | "subproject_1"                                              | _                                                 | "String"
        "configurationMatching"          | toProviderSet(property) | "subproject_2"                                              | _                                                 | "Provider<String>"
        "configurationMatching"          | toSetter(property)      | "subproject_3"                                              | _                                                 | "String"
        "configurationMatching"          | toSetter(property)      | "subproject_4"                                              | _                                                 | "Provider<String>"

        "configurationAttributes"        | toProviderSet(property) | ["config1", "config2"]                                      | _                                                 | "List<String>"
        "configurationAttributes"        | toProviderSet(property) | ["config1", "config2"]                                      | _                                                 | "Provider<List<String>>"
        "configurationAttributes"        | toSetter(property)      | ["config1", "config2"]                                      | _                                                 | "List<String>"
        "configurationAttributes"        | toSetter(property)      | ["config1", "config2"]                                      | _                                                 | "Provider<List<String>>"
        "configurationAttributes"        | property                | ["config1", "config2"]                                      | _                                                 | "List<String>"
        "configurationAttributes"        | property                | "config1"                                                   | ["config1"]                                       | "String"
        "configurationAttributes"        | property                | ["config1", "config2"]                                      | _                                                 | "String..."

        "command"                        | toProviderSet(property) | "command_1"                                                 | _                                                 | "String"
        "command"                        | toProviderSet(property) | "command_2"                                                 | _                                                 | "Provider<String>"
        "command"                        | toSetter(property)      | "command_3"                                                 | _                                                 | "String"
        "command"                        | toSetter(property)      | "command_4"                                                 | _                                                 | "Provider<String>"

        "initScript"                     | toProviderSet(property) | osPath("/path/to/initScript1")                              | _                                                 | "File"
        "initScript"                     | toProviderSet(property) | osPath("/path/to/initScript2")                              | _                                                 | "Provider<RegularFile>"
        "initScript"                     | toSetter(property)      | osPath("/path/to/initScript3")                              | _                                                 | "File"
        "initScript"                     | toSetter(property)      | osPath("/path/to/initScript4")                              | _                                                 | "Provider<RegularFile>"
        "initScript"                     | "initScriptOption"      | osPath("/path/to/initScript5")                              | _                                                 | "String"

        "skipUnresolved"                 | toProviderSet(property) | true                                                        | _                                                 | "Boolean"
        "skipUnresolved"                 | toProviderSet(property) | true                                                        | _                                                 | "Provider<Boolean>"
        "skipUnresolved"                 | toSetter(property)      | true                                                        | _                                                 | "Boolean"
        "skipUnresolved"                 | toSetter(property)      | true                                                        | _                                                 | "Provider<Boolean>"

        "yarnWorkspaces"                 | toProviderSet(property) | true                                                        | _                                                 | "Boolean"
        "yarnWorkspaces"                 | toProviderSet(property) | true                                                        | _                                                 | "Provider<Boolean>"
        "yarnWorkspaces"                 | toSetter(property)      | true                                                        | _                                                 | "Boolean"
        "yarnWorkspaces"                 | toSetter(property)      | true                                                        | _                                                 | "Provider<Boolean>"

        value = wrapValueBasedOnType(rawValue, type, wrapValueFallback)
        expectedValue = returnValue == _ ? rawValue : returnValue
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
    def "composes correct CLI string from #setter -> #expected"() {

        given: "a snyk wrapper"
        setSnykWrapper()

        and: "a set of properties being set onto the task"
        if (setter.concat("file")){
            createFile(mockFile)
        }
        def platformProjectDir = projectDir.path.split("\\\\").join("/")
        setter = setter.replaceAll("#projectDir#", platformProjectDir)
        buildFile << "\n${subjectUnderTestName}.${setter}"

        when:
        def result = runTasksSuccessfully(subjectUnderTestName)
        expected = expected.replaceAll("#projectDir#", escapedPath(projectDir.path))

        then:
        outputContains(result, expected)

        where:
        setter                                                                | flags
        // TestOption
        "allProjects=true"                                                    | "--all-projects"
        "projectName=${wrap("pancakes")}"                                     | "--project-name=pancakes"
        "detectionDepth=7"                                                    | "--detection-depth=7"
        "exclude=[file(${wrap("foo.bar")})]"                                  | "--exclude=${new File("#projectDir#/foo.bar").path}"
        "pruneRepeatedSubDependencies=true"                                   | "--prune-repeated-subdependencies"
        "printDependencies=true"                                              | "--print-deps"
        "remoteRepoUrl=${wrap("foo.bar/pancakes")}"                           | "--remote-repo-url"
        "includeDevelopmentDependencies=true"                                 | "--dev"
        "orgName=${wrap("PANCAKES")}"                                         | "--org=PANCAKES"
        "packageFile = ${wrapValueBasedOnType("#projectDir#/foo.bar", File)}" | "--file=${new File("#projectDir#/foo.bar").path}"
        "ignorePolicy=true"                                                   | "--ignore-policy"
        "showVulnerablePaths=${wrapValueBasedOnType("all", String)}"          | "--show-vulnerable-paths=all"
        "targetReference=${wrapValueBasedOnType("foobar", String)}"           | "--target-reference"
        "policyPath=file(${wrapValueBasedOnType("foo.bar", String)})"         | "--policy-path=${new File("#projectDir#/foo.bar").path}"
        "printJson=true"                                                      | "--json"
        "jsonOutputPath=file(${wrapValueBasedOnType("foo.bar", String)})"     | "--json-file-output=${new File("#projectDir#/foo.bar").path}"
        "printSarif=true"                                                     | "--sarif"
        "sarifOutputPath=file(${wrapValueBasedOnType("foo.bar", String)})"    | "--sarif-file-output=${new File("#projectDir#/foo.bar").path}"
        "severityThreshold=${wrap("critical")}"                               | "--severity-threshold=critical"
        "failOn=${wrap("all")}"                                               | "--fail-on=all"
        // ProjectOption
        "scanAllUnmanaged=true"                                               | "--scan-all-unmanaged"
        "subProject=${wrap("foobar")}"                                        | "--sub-project=foobar"
        "allSubProjects=true"                                                 | "--all-sub-projects"
        "configurationMatching=${wrap("foobar")}"                             | "--configuration-matching=foobar"
        "configurationAttributes=[${wrap("foo")},${wrap("bar")}]"             | "--configuration-attributes=foo,bar"
        "initScript=${wrapValueBasedOnType("#projectDir#/foo.bar", File)}"    | "--gradle-init-script=${new File("#projectDir#/foo.bar").path}"
        "reachable=true"                                                      | "--reachable"
        "reachableTimeout=7"                                                  | "--reachable-timeout=7"
        "assetsProjectName=true"                                              | "--assets-project-name"
        "projectNamePrefix=${wrap("foobar")}"                                 | "--project-name-prefix=foobar"
        "strictOutOfSync=true"                                                | "--strict-out-of-sync"
        "yarnWorkspaces=true"                                                 | "--yarn-workspaces"
        "skipUnresolved=true"                                                 | "--skip-unresolved"
        "command=${wrap("foobar")}"                                           | "--command=foobar"

        expected = flags.toString().empty ? commandName : "${commandName} ${flags}"
        mockFile = "foo.bar"
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
