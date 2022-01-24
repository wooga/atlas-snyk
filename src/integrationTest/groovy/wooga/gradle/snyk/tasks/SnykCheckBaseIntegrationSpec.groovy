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
import wooga.gradle.snyk.cli.SnykCLIOptions

import static com.wooga.gradle.test.PropertyUtils.toProviderSet
import static com.wooga.gradle.test.PropertyUtils.toSetter

abstract class SnykCheckBaseIntegrationSpec<T extends SnykTask> extends SnykTaskIntegrationSpec<T> {
    @Unroll("can set property #property with #method and type #type")
    def "can set property SBSTask"() {
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
        property                         | method                  | rawValue                                        | returnValue      | type
        "allProjects"                    | toProviderSet(property) | true                                            | _                | "Boolean"
        "allProjects"                    | toProviderSet(property) | false                                           | _                | "Provider<Boolean>"
        "allProjects"                    | toSetter(property)      | true                                            | _                | "Boolean"
        "allProjects"                    | toSetter(property)      | false                                           | _                | "Provider<Boolean>"

        "detectionDepth"                 | toProviderSet(property) | 1                                               | _                | "Integer"
        "detectionDepth"                 | toProviderSet(property) | 2                                               | _                | "Provider<Integer>"
        "detectionDepth"                 | toSetter(property)      | 3                                               | _                | "Integer"
        "detectionDepth"                 | toSetter(property)      | 4                                               | _                | "Provider<Integer>"
        "detectionDepth"                 | property                | 4                                               | _                | "String"

        "exclude"                        | toProviderSet(property) | ["/path/to/dir"]                                | _                | "List<Directory>"
        "exclude"                        | toProviderSet(property) | ["/path/to/dir"]                                | _                | "Provider<List<Directory>>"
        "exclude"                        | toSetter(property)      | ["/path/to/dir"]                                | _                | "List<Directory>"
        "exclude"                        | toSetter(property)      | ["/path/to/dir"]                                | _                | "Provider<List<Directory>>"
        "exclude"                        | property                | ["/path/to/dir"]                                | _                | "List<Directory>"
        "exclude"                        | property                | "/path/to/dir"                                  | ['/path/to/dir'] | "Directory"
        "exclude"                        | property                | ["/path/to/dir"]                                | _                | "Directory..."
        "exclude"                        | "excludeOption"         | ["/path/to/dir"]                                | _                | "List<String>"

        "pruneRepeatedSubDependencies"   | toProviderSet(property) | true                                            | _                | "Boolean"
        "pruneRepeatedSubDependencies"   | toProviderSet(property) | false                                           | _                | "Provider<Boolean>"
        "pruneRepeatedSubDependencies"   | toSetter(property)      | true                                            | _                | "Boolean"
        "pruneRepeatedSubDependencies"   | toSetter(property)      | false                                           | _                | "Provider<Boolean>"

        "printDependencies"              | toProviderSet(property) | true                                            | _                | "Boolean"
        "printDependencies"              | toProviderSet(property) | false                                           | _                | "Provider<Boolean>"
        "printDependencies"              | toSetter(property)      | true                                            | _                | "Boolean"
        "printDependencies"              | toSetter(property)      | false                                           | _                | "Provider<Boolean>"

        "remoteRepoUrl"                  | toProviderSet(property) | "http://remote/url/1"                           | _                | "String"
        "remoteRepoUrl"                  | toProviderSet(property) | "http://remote/url/2"                           | _                | "Provider<String>"
        "remoteRepoUrl"                  | toSetter(property)      | "http://remote/url/3"                           | _                | "String"
        "remoteRepoUrl"                  | toSetter(property)      | "http://remote/url/4"                           | _                | "Provider<String>"

        "includeDevelopmentDependencies" | toProviderSet(property) | true                                            | _                | "Boolean"
        "includeDevelopmentDependencies" | toProviderSet(property) | false                                           | _                | "Provider<Boolean>"
        "includeDevelopmentDependencies" | toSetter(property)      | true                                            | _                | "Boolean"
        "includeDevelopmentDependencies" | toSetter(property)      | false                                           | _                | "Provider<Boolean>"

        "orgName"                        | toProviderSet(property) | "org1"                                          | _                | "String"
        "orgName"                        | toProviderSet(property) | "org2"                                          | _                | "Provider<String>"
        "orgName"                        | toSetter(property)      | "org3"                                          | _                | "String"
        "orgName"                        | toSetter(property)      | "org4"                                          | _                | "Provider<String>"

        "packageFile"                    | toProviderSet(property) | "/path/to/file1"                                | _                | "File"
        "packageFile"                    | toProviderSet(property) | "/path/to/file2"                                | _                | "Provider<RegularFile>"
        "packageFile"                    | toSetter(property)      | "/path/to/file3"                                | _                | "File"
        "packageFile"                    | toSetter(property)      | "/path/to/file4"                                | _                | "Provider<RegularFile>"
        "packageFile"                    | property                | "/path/to/file5"                                | _                | "String"

        "ignorePolicy"                   | toProviderSet(property) | true                                            | _                | "Boolean"
        "ignorePolicy"                   | toProviderSet(property) | false                                           | _                | "Provider<Boolean>"
        "ignorePolicy"                   | toSetter(property)      | true                                            | _                | "Boolean"
        "ignorePolicy"                   | toSetter(property)      | false                                           | _                | "Provider<Boolean>"

        "showVulnerablePaths"            | toProviderSet(property) | SnykCLIOptions.VulnerablePathsOption.some       | _                | "VulnerablePathsOption"
        "showVulnerablePaths"            | toProviderSet(property) | SnykCLIOptions.VulnerablePathsOption.none       | _                | "Provider<VulnerablePathsOption>"
        "showVulnerablePaths"            | toSetter(property)      | SnykCLIOptions.VulnerablePathsOption.all        | _                | "VulnerablePathsOption"
        "showVulnerablePaths"            | toSetter(property)      | SnykCLIOptions.VulnerablePathsOption.some       | _                | "Provider<VulnerablePathsOption>"

        "projectName"                    | toProviderSet(property) | "test1"                                         | _                | "String"
        "projectName"                    | toProviderSet(property) | "test2"                                         | _                | "Provider<String>"
        "projectName"                    | toSetter(property)      | "test3"                                         | _                | "String"
        "projectName"                    | toSetter(property)      | "test4"                                         | _                | "Provider<String>"

        "targetReference"                | toProviderSet(property) | "test_reference_1"                              | _                | "String"
        "targetReference"                | toProviderSet(property) | "test_reference_2"                              | _                | "Provider<String>"
        "targetReference"                | toSetter(property)      | "test_reference_3"                              | _                | "String"
        "targetReference"                | toSetter(property)      | "test_reference_4"                              | _                | "Provider<String>"

        "policyPath"                     | toProviderSet(property) | "/path/to/file1"                                | _                | "File"
        "policyPath"                     | toProviderSet(property) | "/path/to/file2"                                | _                | "Provider<RegularFile>"
        "policyPath"                     | toSetter(property)      | "/path/to/file3"                                | _                | "File"
        "policyPath"                     | toSetter(property)      | "/path/to/file4"                                | _                | "Provider<RegularFile>"
        "policyPath"                     | property                | "/path/to/file5"                                | _                | "String"

        "printJson"                      | toProviderSet(property) | true                                            | _                | "Boolean"
        "printJson"                      | toProviderSet(property) | true                                            | _                | "Provider<Boolean>"
        "printJson"                      | toSetter(property)      | true                                            | _                | "Boolean"
        "printJson"                      | toSetter(property)      | true                                            | _                | "Provider<Boolean>"

        "jsonOutputPath"                 | toProviderSet(property) | "/path/to/file1"                                | _                | "File"
        "jsonOutputPath"                 | toProviderSet(property) | "/path/to/file2"                                | _                | "Provider<RegularFile>"
        "jsonOutputPath"                 | toSetter(property)      | "/path/to/file3"                                | _                | "File"
        "jsonOutputPath"                 | toSetter(property)      | "/path/to/file4"                                | _                | "Provider<RegularFile>"
        "jsonOutputPath"                 | property                | "/path/to/file5"                                | _                | "String"

        "printSarif"                     | toProviderSet(property) | true                                            | _                | "Boolean"
        "printSarif"                     | toProviderSet(property) | true                                            | _                | "Provider<Boolean>"
        "printSarif"                     | toSetter(property)      | true                                            | _                | "Boolean"
        "printSarif"                     | toSetter(property)      | true                                            | _                | "Provider<Boolean>"

        "sarifOutputPath"                | toProviderSet(property) | "/path/to/file1"                                | _                | "File"
        "sarifOutputPath"                | toProviderSet(property) | "/path/to/file2"                                | _                | "Provider<RegularFile>"
        "sarifOutputPath"                | toSetter(property)      | "/path/to/file3"                                | _                | "File"
        "sarifOutputPath"                | toSetter(property)      | "/path/to/file4"                                | _                | "Provider<RegularFile>"
        "sarifOutputPath"                | property                | "/path/to/file5"                                | _                | "String"

        "severityThreshold"              | toProviderSet(property) | SnykCLIOptions.SeverityThresholdOption.low      | _                | "SeverityThresholdOption"
        "severityThreshold"              | toProviderSet(property) | SnykCLIOptions.SeverityThresholdOption.medium   | _                | "Provider<SeverityThresholdOption>"
        "severityThreshold"              | toSetter(property)      | SnykCLIOptions.SeverityThresholdOption.high     | _                | "SeverityThresholdOption"
        "severityThreshold"              | toSetter(property)      | SnykCLIOptions.SeverityThresholdOption.critical | _                | "Provider<SeverityThresholdOption>"

        "failOn"                         | toProviderSet(property) | SnykCLIOptions.FailOnOption.all                 | _                | "FailOnOption"
        "failOn"                         | toProviderSet(property) | SnykCLIOptions.FailOnOption.upgradable          | _                | "Provider<FailOnOption>"
        "failOn"                         | toSetter(property)      | SnykCLIOptions.FailOnOption.patchable           | _                | "FailOnOption"
        "failOn"                         | toSetter(property)      | SnykCLIOptions.FailOnOption.all                 | _                | "Provider<FailOnOption>"


        "compilerArguments"              | toProviderSet(property) | ["--foo", "--bar"]                              | _                | "List<String>"
        "compilerArguments"              | toProviderSet(property) | ["--foo", "--bar"]                              | _                | "Provider<List<String>>"
        "compilerArguments"              | toSetter(property)      | ["--foo", "--bar"]                              | _                | "List<String>"
        "compilerArguments"              | toSetter(property)      | ["--foo", "--bar"]                              | _                | "Provider<List<String>>"
        "compilerArguments"              | property                | ["--foo", "--bar"]                              | _                | "List<String>"
        "compilerArguments"              | property                | "--foo"                                         | ["--foo"]        | "String"
        "compilerArguments"              | property                | ["--foo", "--bar"]                              | _                | "String..."

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
        "--init-script"                    | _
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
}
