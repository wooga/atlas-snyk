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

package wooga.gradle.snyk

import com.wooga.gradle.test.PropertyLocation
import com.wooga.gradle.test.PropertyQueryTaskWriter
import spock.lang.Unroll

import static com.wooga.gradle.test.PropertyUtils.*
import static com.wooga.gradle.test.SpecUtils.escapedPath
import static wooga.gradle.snyk.cli.SnykCLIOptions.*

class SnykPluginIntegrationSpec extends SnykIntegrationSpec {

    def setup() {
        buildFile << """
        ${applyPlugin(SnykPlugin)}
        """
    }

    @Unroll
    def "extension property :#property returns '#testValue' if #reason"() {
        given: "a set value"
        switch (location) {
            case PropertyLocation.script:
                buildFile << "${extensionName}.${invocation}"
                break
            case PropertyLocation.property:
                def propertiesFile = createFile("gradle.properties")
                propertiesFile << "${extensionName}.${property} = ${escapedValue}"
                break
            case PropertyLocation.environment:
                def envPropertyKey = envNameFromProperty(extensionName, property)
                environmentVariables.set(envPropertyKey, value.toString())
                break
            default:
                break
        }

        and: "the test value with replace placeholders"
        if (testValue instanceof String) {
            testValue = testValue.replaceAll("#projectDir#", escapedPath(projectDir.path))
        }

        when:
        def query = new PropertyQueryTaskWriter("${extensionName}.${property}")
        query.write(buildFile)
        def result = runTasksSuccessfully(query.taskName)

        then:
        query.matches(result, testValue)

        where:
        property                         | method                  | rawValue                                                        | expectedValue                                                          | type                                        | location
        "allProjects"                    | _                       | _                                                               | false                                                                  | "Provider<Boolean>"                         | PropertyLocation.none
        "allProjects"                    | _                       | "true"                                                          | true                                                                   | _                                           | PropertyLocation.environment
        "allProjects"                    | _                       | "true"                                                          | true                                                                   | _                                           | PropertyLocation.property
        "allProjects"                    | toSetter(property)      | true                                                            | _                                                                      | "Provider<Boolean>"                         | PropertyLocation.script
        "allProjects"                    | toSetter(property)      | false                                                           | _                                                                      | "Boolean"                                   | PropertyLocation.script
        "allProjects"                    | toProviderSet(property) | true                                                            | _                                                                      | "Provider<Boolean>"                         | PropertyLocation.script
        "allProjects"                    | toProviderSet(property) | false                                                           | _                                                                      | "Boolean"                                   | PropertyLocation.script

        "detectionDepth"                 | _                       | _                                                               | null                                                                   | "Provider<Integer>"                         | PropertyLocation.none
        "detectionDepth"                 | _                       | 1                                                               | _                                                                      | _                                           | PropertyLocation.environment
        "detectionDepth"                 | _                       | 2                                                               | _                                                                      | _                                           | PropertyLocation.property
        "detectionDepth"                 | toSetter(property)      | 3                                                               | _                                                                      | "Provider<Integer>"                         | PropertyLocation.script
        "detectionDepth"                 | toSetter(property)      | 4                                                               | _                                                                      | "Integer"                                   | PropertyLocation.script
        "detectionDepth"                 | toProviderSet(property) | 5                                                               | _                                                                      | "Provider<Integer>"                         | PropertyLocation.script
        "detectionDepth"                 | toProviderSet(property) | 6                                                               | _                                                                      | "Integer"                                   | PropertyLocation.script

        "exclude"                        | _                       | _                                                               | null                                                                   | "Provider<List<Directory>>"                 | PropertyLocation.none
        "exclude"                        | _                       | '/path/one,/path/two'                                           | ['/path/one', '/path/two']                                             | _                                           | PropertyLocation.environment
        "exclude"                        | _                       | '/path/one,/path/two'                                           | ['/path/one', '/path/two']                                             | _                                           | PropertyLocation.property
        "exclude"                        | toSetter(property)      | ['/path/one', '/path/two']                                      | _                                                                      | "Provider<List<Directory>>"                 | PropertyLocation.script
        "exclude"                        | toSetter(property)      | ['/path/one', '/path/two']                                      | _                                                                      | "List<Directory>"                           | PropertyLocation.script
        "exclude"                        | toProviderSet(property) | ['/path/one', '/path/two']                                      | _                                                                      | "Provider<List<Directory>>"                 | PropertyLocation.script
        "exclude"                        | toProviderSet(property) | ['/path/one', '/path/two']                                      | _                                                                      | "List<Directory>"                           | PropertyLocation.script

        "pruneRepeatedSubDependencies"   | _                       | _                                                               | false                                                                  | "Provider<Boolean>"                         | PropertyLocation.none
        "pruneRepeatedSubDependencies"   | _                       | "true"                                                          | true                                                                   | _                                           | PropertyLocation.environment
        "pruneRepeatedSubDependencies"   | _                       | "true"                                                          | true                                                                   | _                                           | PropertyLocation.property
        "pruneRepeatedSubDependencies"   | toSetter(property)      | true                                                            | _                                                                      | "Provider<Boolean>"                         | PropertyLocation.script
        "pruneRepeatedSubDependencies"   | toSetter(property)      | false                                                           | _                                                                      | "Boolean"                                   | PropertyLocation.script
        "pruneRepeatedSubDependencies"   | toProviderSet(property) | true                                                            | _                                                                      | "Provider<Boolean>"                         | PropertyLocation.script
        "pruneRepeatedSubDependencies"   | toProviderSet(property) | false                                                           | _                                                                      | "Boolean"                                   | PropertyLocation.script

        "printDependencies"              | _                       | _                                                               | false                                                                  | "Provider<Boolean>"                         | PropertyLocation.none
        "printDependencies"              | _                       | "true"                                                          | true                                                                   | _                                           | PropertyLocation.environment
        "printDependencies"              | _                       | "true"                                                          | true                                                                   | _                                           | PropertyLocation.property
        "printDependencies"              | toSetter(property)      | true                                                            | _                                                                      | "Provider<Boolean>"                         | PropertyLocation.script
        "printDependencies"              | toSetter(property)      | false                                                           | _                                                                      | "Boolean"                                   | PropertyLocation.script
        "printDependencies"              | toProviderSet(property) | true                                                            | _                                                                      | "Provider<Boolean>"                         | PropertyLocation.script
        "printDependencies"              | toProviderSet(property) | false                                                           | _                                                                      | "Boolean"                                   | PropertyLocation.script

        "remoteRepoUrl"                  | _                       | _                                                               | null                                                                   | "Provider<String>"                          | PropertyLocation.none
        "remoteRepoUrl"                  | _                       | "http://some/remote/1"                                          | _                                                                      | _                                           | PropertyLocation.environment
        "remoteRepoUrl"                  | _                       | "http://some/remote/2"                                          | _                                                                      | _                                           | PropertyLocation.property
        "remoteRepoUrl"                  | toSetter(property)      | "http://some/remote/3"                                          | _                                                                      | "Provider<String>"                          | PropertyLocation.script
        "remoteRepoUrl"                  | toSetter(property)      | "http://some/remote/4"                                          | _                                                                      | "String"                                    | PropertyLocation.script
        "remoteRepoUrl"                  | toProviderSet(property) | "http://some/remote/5"                                          | _                                                                      | "Provider<String>"                          | PropertyLocation.script
        "remoteRepoUrl"                  | toProviderSet(property) | "http://some/remote/6"                                          | _                                                                      | "String"                                    | PropertyLocation.script

        "includeDevelopmentDependencies" | _                       | _                                                               | false                                                                  | "Provider<Boolean>"                         | PropertyLocation.none
        "includeDevelopmentDependencies" | _                       | "true"                                                          | true                                                                   | _                                           | PropertyLocation.environment
        "includeDevelopmentDependencies" | _                       | "true"                                                          | true                                                                   | _                                           | PropertyLocation.property
        "includeDevelopmentDependencies" | toSetter(property)      | true                                                            | _                                                                      | "Provider<Boolean>"                         | PropertyLocation.script
        "includeDevelopmentDependencies" | toSetter(property)      | false                                                           | _                                                                      | "Boolean"                                   | PropertyLocation.script
        "includeDevelopmentDependencies" | toProviderSet(property) | true                                                            | _                                                                      | "Provider<Boolean>"                         | PropertyLocation.script
        "includeDevelopmentDependencies" | toProviderSet(property) | false                                                           | _                                                                      | "Boolean"                                   | PropertyLocation.script

        "orgName"                        | _                       | _                                                               | null                                                                   | "Provider<String>"                          | PropertyLocation.none
        "orgName"                        | _                       | "org1"                                                          | _                                                                      | _                                           | PropertyLocation.environment
        "orgName"                        | _                       | "org2"                                                          | _                                                                      | _                                           | PropertyLocation.property
        "orgName"                        | toSetter(property)      | "org3"                                                          | _                                                                      | "Provider<String>"                          | PropertyLocation.script
        "orgName"                        | toSetter(property)      | "org4"                                                          | _                                                                      | "String"                                    | PropertyLocation.script
        "orgName"                        | toProviderSet(property) | "org5"                                                          | _                                                                      | "Provider<String>"                          | PropertyLocation.script
        "orgName"                        | toProviderSet(property) | "org6"                                                          | _                                                                      | "String"                                    | PropertyLocation.script

        "packageFile"                    | _                       | _                                                               | null                                                                   | "Provider<RegularFile>"                     | PropertyLocation.none
        "packageFile"                    | _                       | "/path/to/package1"                                             | _                                                                      | _                                           | PropertyLocation.environment
        "packageFile"                    | _                       | "/path/to/package2"                                             | _                                                                      | _                                           | PropertyLocation.property
        "packageFile"                    | toSetter(property)      | "/path/to/package3"                                             | _                                                                      | "Provider<RegularFile>"                     | PropertyLocation.script
        "packageFile"                    | toSetter(property)      | "/path/to/package4"                                             | _                                                                      | "File"                                      | PropertyLocation.script
        "packageFile"                    | toProviderSet(property) | "/path/to/package5"                                             | _                                                                      | "Provider<RegularFile>"                     | PropertyLocation.script
        "packageFile"                    | toProviderSet(property) | "/path/to/package6"                                             | _                                                                      | "File"                                      | PropertyLocation.script

        "ignorePolicy"                   | _                       | _                                                               | false                                                                  | "Provider<Boolean>"                         | PropertyLocation.none
        "ignorePolicy"                   | _                       | "true"                                                          | true                                                                   | _                                           | PropertyLocation.environment
        "ignorePolicy"                   | _                       | "true"                                                          | true                                                                   | _                                           | PropertyLocation.property
        "ignorePolicy"                   | toSetter(property)      | true                                                            | _                                                                      | "Provider<Boolean>"                         | PropertyLocation.script
        "ignorePolicy"                   | toSetter(property)      | false                                                           | _                                                                      | "Boolean"                                   | PropertyLocation.script
        "ignorePolicy"                   | toProviderSet(property) | true                                                            | _                                                                      | "Provider<Boolean>"                         | PropertyLocation.script
        "ignorePolicy"                   | toProviderSet(property) | false                                                           | _                                                                      | "Boolean"                                   | PropertyLocation.script

        "showVulnerablePaths"            | _                       | _                                                               | null                                                                   | "Provider<VulnerablePathsOption>"           | PropertyLocation.none
        "showVulnerablePaths"            | _                       | VulnerablePathsOption.none                                      | _                                                                      | _                                           | PropertyLocation.environment
        "showVulnerablePaths"            | _                       | VulnerablePathsOption.some                                      | _                                                                      | _                                           | PropertyLocation.property
        "showVulnerablePaths"            | toSetter(property)      | VulnerablePathsOption.all                                       | _                                                                      | "Provider<VulnerablePathsOption>"           | PropertyLocation.script
        "showVulnerablePaths"            | toSetter(property)      | VulnerablePathsOption.none                                      | _                                                                      | "VulnerablePathsOption"                     | PropertyLocation.script
        "showVulnerablePaths"            | toProviderSet(property) | VulnerablePathsOption.some                                      | _                                                                      | "Provider<VulnerablePathsOption>"           | PropertyLocation.script
        "showVulnerablePaths"            | toProviderSet(property) | VulnerablePathsOption.all                                       | _                                                                      | "VulnerablePathsOption"                     | PropertyLocation.script

        "projectName"                    | _                       | _                                                               | null                                                                   | "Provider<String>"                          | PropertyLocation.none
        "projectName"                    | _                       | "name1"                                                         | _                                                                      | _                                           | PropertyLocation.environment
        "projectName"                    | _                       | "name2"                                                         | _                                                                      | _                                           | PropertyLocation.property
        "projectName"                    | toSetter(property)      | "name3"                                                         | _                                                                      | "Provider<String>"                          | PropertyLocation.script
        "projectName"                    | toSetter(property)      | "name4"                                                         | _                                                                      | "String"                                    | PropertyLocation.script
        "projectName"                    | toProviderSet(property) | "name5"                                                         | _                                                                      | "Provider<String>"                          | PropertyLocation.script
        "projectName"                    | toProviderSet(property) | "name6"                                                         | _                                                                      | "String"                                    | PropertyLocation.script

        "targetReference"                | _                       | _                                                               | null                                                                   | "Provider<String>"                          | PropertyLocation.none
        "targetReference"                | _                       | "name1"                                                         | _                                                                      | _                                           | PropertyLocation.environment
        "targetReference"                | _                       | "name2"                                                         | _                                                                      | _                                           | PropertyLocation.property
        "targetReference"                | toSetter(property)      | "name3"                                                         | _                                                                      | "Provider<String>"                          | PropertyLocation.script
        "targetReference"                | toSetter(property)      | "name4"                                                         | _                                                                      | "String"                                    | PropertyLocation.script
        "targetReference"                | toProviderSet(property) | "name5"                                                         | _                                                                      | "Provider<String>"                          | PropertyLocation.script
        "targetReference"                | toProviderSet(property) | "name6"                                                         | _                                                                      | "String"                                    | PropertyLocation.script

        "policyPath"                     | _                       | _                                                               | null                                                                   | "Provider<RegularFile>"                     | PropertyLocation.none
        "policyPath"                     | _                       | "/path/to/policy1"                                              | _                                                                      | _                                           | PropertyLocation.environment
        "policyPath"                     | _                       | "/path/to/policy2"                                              | _                                                                      | _                                           | PropertyLocation.property
        "policyPath"                     | toSetter(property)      | "/path/to/policy3"                                              | _                                                                      | "Provider<RegularFile>"                     | PropertyLocation.script
        "policyPath"                     | toSetter(property)      | "/path/to/policy4"                                              | _                                                                      | "File"                                      | PropertyLocation.script
        "policyPath"                     | toProviderSet(property) | "/path/to/policy5"                                              | _                                                                      | "Provider<RegularFile>"                     | PropertyLocation.script
        "policyPath"                     | toProviderSet(property) | "/path/to/policy6"                                              | _                                                                      | "File"                                      | PropertyLocation.script

        "printJson"                      | _                       | _                                                               | false                                                                  | "Provider<Boolean>"                         | PropertyLocation.none
        "printJson"                      | _                       | "true"                                                          | true                                                                   | _                                           | PropertyLocation.environment
        "printJson"                      | _                       | "true"                                                          | true                                                                   | _                                           | PropertyLocation.property
        "printJson"                      | toSetter(property)      | true                                                            | _                                                                      | "Provider<Boolean>"                         | PropertyLocation.script
        "printJson"                      | toSetter(property)      | false                                                           | _                                                                      | "Boolean"                                   | PropertyLocation.script
        "printJson"                      | toProviderSet(property) | true                                                            | _                                                                      | "Provider<Boolean>"                         | PropertyLocation.script
        "printJson"                      | toProviderSet(property) | false                                                           | _                                                                      | "Boolean"                                   | PropertyLocation.script

        "jsonOutputPath"                 | _                       | _                                                               | null                                                                   | "Provider<RegularFile>"                     | PropertyLocation.none
        "jsonOutputPath"                 | _                       | "/path/to/json1"                                                | _                                                                      | _                                           | PropertyLocation.environment
        "jsonOutputPath"                 | _                       | "/path/to/json2"                                                | _                                                                      | _                                           | PropertyLocation.property
        "jsonOutputPath"                 | toSetter(property)      | "/path/to/json3"                                                | _                                                                      | "Provider<RegularFile>"                     | PropertyLocation.script
        "jsonOutputPath"                 | toSetter(property)      | "/path/to/json4"                                                | _                                                                      | "File"                                      | PropertyLocation.script
        "jsonOutputPath"                 | toProviderSet(property) | "/path/to/json5"                                                | _                                                                      | "Provider<RegularFile>"                     | PropertyLocation.script
        "jsonOutputPath"                 | toProviderSet(property) | "/path/to/json6"                                                | _                                                                      | "File"                                      | PropertyLocation.script

        "printSarif"                     | _                       | _                                                               | false                                                                  | "Provider<Boolean>"                         | PropertyLocation.none
        "printSarif"                     | _                       | "true"                                                          | true                                                                   | _                                           | PropertyLocation.environment
        "printSarif"                     | _                       | "true"                                                          | true                                                                   | _                                           | PropertyLocation.property
        "printSarif"                     | toSetter(property)      | true                                                            | _                                                                      | "Provider<Boolean>"                         | PropertyLocation.script
        "printSarif"                     | toSetter(property)      | false                                                           | _                                                                      | "Boolean"                                   | PropertyLocation.script
        "printSarif"                     | toProviderSet(property) | true                                                            | _                                                                      | "Provider<Boolean>"                         | PropertyLocation.script
        "printSarif"                     | toProviderSet(property) | false                                                           | _                                                                      | "Boolean"                                   | PropertyLocation.script

        "sarifOutputPath"                | _                       | _                                                               | null                                                                   | "Provider<RegularFile>"                     | PropertyLocation.none
        "sarifOutputPath"                | _                       | "/path/to/sarif1"                                               | _                                                                      | _                                           | PropertyLocation.environment
        "sarifOutputPath"                | _                       | "/path/to/sarif2"                                               | _                                                                      | _                                           | PropertyLocation.property
        "sarifOutputPath"                | toSetter(property)      | "/path/to/sarif3"                                               | _                                                                      | "Provider<RegularFile>"                     | PropertyLocation.script
        "sarifOutputPath"                | toSetter(property)      | "/path/to/sarif4"                                               | _                                                                      | "File"                                      | PropertyLocation.script
        "sarifOutputPath"                | toProviderSet(property) | "/path/to/sarif5"                                               | _                                                                      | "Provider<RegularFile>"                     | PropertyLocation.script
        "sarifOutputPath"                | toProviderSet(property) | "/path/to/sarif6"                                               | _                                                                      | "File"                                      | PropertyLocation.script

        "severityThreshold"              | _                       | _                                                               | null                                                                   | "Provider<SeverityThresholdOption>"         | PropertyLocation.none
        "severityThreshold"              | _                       | SeverityThresholdOption.low                                     | _                                                                      | _                                           | PropertyLocation.environment
        "severityThreshold"              | _                       | SeverityThresholdOption.medium                                  | _                                                                      | _                                           | PropertyLocation.property
        "severityThreshold"              | toSetter(property)      | SeverityThresholdOption.high                                    | _                                                                      | "Provider<SeverityThresholdOption>"         | PropertyLocation.script
        "severityThreshold"              | toSetter(property)      | SeverityThresholdOption.critical                                | _                                                                      | "SeverityThresholdOption"                   | PropertyLocation.script
        "severityThreshold"              | toProviderSet(property) | SeverityThresholdOption.low                                     | _                                                                      | "Provider<SeverityThresholdOption>"         | PropertyLocation.script
        "severityThreshold"              | toProviderSet(property) | SeverityThresholdOption.medium                                  | _                                                                      | "SeverityThresholdOption"                   | PropertyLocation.script

        "failOn"                         | _                       | _                                                               | null                                                                   | "Provider<FailOnOption>"                    | PropertyLocation.none
        "failOn"                         | _                       | FailOnOption.all                                                | _                                                                      | _                                           | PropertyLocation.environment
        "failOn"                         | _                       | FailOnOption.upgradable                                         | _                                                                      | _                                           | PropertyLocation.property
        "failOn"                         | toSetter(property)      | FailOnOption.patchable                                          | _                                                                      | "Provider<FailOnOption>"                    | PropertyLocation.script
        "failOn"                         | toSetter(property)      | FailOnOption.all                                                | _                                                                      | "FailOnOption"                              | PropertyLocation.script
        "failOn"                         | toProviderSet(property) | FailOnOption.upgradable                                         | _                                                                      | "Provider<FailOnOption>"                    | PropertyLocation.script
        "failOn"                         | toProviderSet(property) | FailOnOption.patchable                                          | _                                                                      | "FailOnOption"                              | PropertyLocation.script

        "compilerArguments"              | _                       | _                                                               | null                                                                   | "Provider<List<String>>"                    | PropertyLocation.none
        "compilerArguments"              | _                       | '--foo --bar=FOO'                                               | ['--foo', '--bar=FOO']                                                 | _                                           | PropertyLocation.environment
        "compilerArguments"              | _                       | '--foo --bar=FOO'                                               | ['--foo', '--bar=FOO']                                                 | _                                           | PropertyLocation.property
        "compilerArguments"              | toSetter(property)      | ['--foo', '--bar=FOO']                                          | _                                                                      | "Provider<List<String>>"                    | PropertyLocation.script
        "compilerArguments"              | toSetter(property)      | ['--foo', '--bar=FOO']                                          | _                                                                      | "List<String>"                              | PropertyLocation.script
        "compilerArguments"              | toProviderSet(property) | ['--foo', '--bar=FOO']                                          | _                                                                      | "Provider<List<String>>"                    | PropertyLocation.script
        "compilerArguments"              | toProviderSet(property) | ['--foo', '--bar=FOO']                                          | _                                                                      | "List<String>"                              | PropertyLocation.script

        "trustPolicies"                  | _                       | _                                                               | false                                                                  | "Provider<Boolean>"                         | PropertyLocation.none
        "trustPolicies"                  | _                       | "true"                                                          | true                                                                   | _                                           | PropertyLocation.environment
        "trustPolicies"                  | _                       | "true"                                                          | true                                                                   | _                                           | PropertyLocation.property
        "trustPolicies"                  | toSetter(property)      | true                                                            | _                                                                      | "Provider<Boolean>"                         | PropertyLocation.script
        "trustPolicies"                  | toSetter(property)      | false                                                           | _                                                                      | "Boolean"                                   | PropertyLocation.script
        "trustPolicies"                  | toProviderSet(property) | true                                                            | _                                                                      | "Provider<Boolean>"                         | PropertyLocation.script
        "trustPolicies"                  | toProviderSet(property) | false                                                           | _                                                                      | "Boolean"                                   | PropertyLocation.script

        "projectEnvironment"             | _                       | _                                                               | null                                                                   | "Provider<List<EnvironmentOption>>"         | PropertyLocation.none
        "projectEnvironment"             | _                       | 'distributed,frontend'                                          | [EnvironmentOption.distributed, EnvironmentOption.frontend]            | _                                           | PropertyLocation.environment
        "projectEnvironment"             | _                       | 'backend,internal'                                              | [EnvironmentOption.backend, EnvironmentOption.internal]                | _                                           | PropertyLocation.property
        "projectEnvironment"             | toSetter(property)      | [EnvironmentOption.frontend, EnvironmentOption.backend]         | _                                                                      | "Provider<List<EnvironmentOption>>"         | PropertyLocation.script
        "projectEnvironment"             | toSetter(property)      | [EnvironmentOption.internal, EnvironmentOption.external]        | _                                                                      | "List<EnvironmentOption>"                   | PropertyLocation.script
        "projectEnvironment"             | toProviderSet(property) | [EnvironmentOption.saas, EnvironmentOption.backend]             | _                                                                      | "Provider<List<EnvironmentOption>>"         | PropertyLocation.script
        "projectEnvironment"             | toProviderSet(property) | [EnvironmentOption.mobile, EnvironmentOption.backend]           | _                                                                      | "List<EnvironmentOption>"                   | PropertyLocation.script

        "projectLifecycle"               | _                       | _                                                               | null                                                                   | "Provider<List<LifecycleOption>>"           | PropertyLocation.none
        "projectLifecycle"               | _                       | 'production,sandbox'                                            | [LifecycleOption.production, LifecycleOption.sandbox]                  | _                                           | PropertyLocation.environment
        "projectLifecycle"               | _                       | 'development,production'                                        | [LifecycleOption.development, LifecycleOption.production]              | _                                           | PropertyLocation.property
        "projectLifecycle"               | toSetter(property)      | [LifecycleOption.development, LifecycleOption.production]       | _                                                                      | "Provider<List<LifecycleOption>>"           | PropertyLocation.script
        "projectLifecycle"               | toSetter(property)      | [LifecycleOption.development, LifecycleOption.production]       | _                                                                      | "List<LifecycleOption>"                     | PropertyLocation.script
        "projectLifecycle"               | toProviderSet(property) | [LifecycleOption.development, LifecycleOption.production]       | _                                                                      | "Provider<List<LifecycleOption>>"           | PropertyLocation.script
        "projectLifecycle"               | toProviderSet(property) | [LifecycleOption.development, LifecycleOption.production]       | _                                                                      | "List<LifecycleOption>"                     | PropertyLocation.script

        "projectBusinessCriticality"     | _                       | _                                                               | null                                                                   | "Provider<List<BusinessCriticalityOption>>" | PropertyLocation.none
        "projectBusinessCriticality"     | _                       | 'critical,medium'                                               | [BusinessCriticalityOption.critical, BusinessCriticalityOption.medium] | _                                           | PropertyLocation.environment
        "projectBusinessCriticality"     | _                       | 'medium,low'                                                    | [BusinessCriticalityOption.medium, BusinessCriticalityOption.low]      | _                                           | PropertyLocation.property
        "projectBusinessCriticality"     | toSetter(property)      | [BusinessCriticalityOption.high, BusinessCriticalityOption.low] | _                                                                      | "Provider<List<BusinessCriticalityOption>>" | PropertyLocation.script
        "projectBusinessCriticality"     | toSetter(property)      | [BusinessCriticalityOption.high, BusinessCriticalityOption.low] | _                                                                      | "List<BusinessCriticalityOption>"           | PropertyLocation.script
        "projectBusinessCriticality"     | toProviderSet(property) | [BusinessCriticalityOption.high, BusinessCriticalityOption.low] | _                                                                      | "Provider<List<BusinessCriticalityOption>>" | PropertyLocation.script
        "projectBusinessCriticality"     | toProviderSet(property) | [BusinessCriticalityOption.high, BusinessCriticalityOption.low] | _                                                                      | "List<BusinessCriticalityOption>"           | PropertyLocation.script

        "projectTags"                    | _                       | _                                                               | null                                                                   | _                                           | PropertyLocation.none
        "projectTags"                    | _                       | "foo= test, bar=baz"                                            | ["foo": "test", "bar": "baz"]                                          | _                                           | PropertyLocation.environment
        "projectTags"                    | _                       | " foo=test,bar= baz "                                           | ["foo": "test", "bar": "baz"]                                          | _                                           | PropertyLocation.property
        "projectTags"                    | toSetter(property)      | ["foo": "test", "bar": "baz"]                                   | _                                                                      | "Provider<Map<String, String>>"             | PropertyLocation.script
        "projectTags"                    | toSetter(property)      | ["foo": "test", "bar": "baz"]                                   | _                                                                      | "Map<String, String>"                       | PropertyLocation.script
        "projectTags"                    | toProviderSet(property) | ["foo": "test", "bar": "baz"]                                   | _                                                                      | "Provider<Map<String, String>>"             | PropertyLocation.script
        "projectTags"                    | toProviderSet(property) | ["foo": "test", "bar": "baz"]                                   | _                                                                      | "Map<String, String>"                       | PropertyLocation.script

        "token"                          | _                       | _                                                               | null                                                                   | "Provider<String>"                          | PropertyLocation.none
        "token"                          | _                       | "test_token1"                                                   | _                                                                      | _                                           | PropertyLocation.environment
        "token"                          | _                       | "test_token2"                                                   | _                                                                      | _                                           | PropertyLocation.property
        "token"                          | toSetter(property)      | "test_token3"                                                   | _                                                                      | "Provider<String>"                          | PropertyLocation.script
        "token"                          | toSetter(property)      | "test_token4"                                                   | _                                                                      | "String"                                    | PropertyLocation.script
        "token"                          | toProviderSet(property) | "test_token5"                                                   | _                                                                      | "Provider<String>"                          | PropertyLocation.script
        "token"                          | toProviderSet(property) | "test_token6"                                                   | _                                                                      | "String"                                    | PropertyLocation.script

        "executable"                     | _                       | "snyk"                                                          | _                                                                      | "Provider<String>"                          | PropertyLocation.none
        "executable"                     | _                       | "snyk_1"                                                        | _                                                                      | _                                           | PropertyLocation.environment
        "executable"                     | _                       | "snyk_2"                                                        | _                                                                      | _                                           | PropertyLocation.property
        "executable"                     | toSetter(property)      | "snyk_3"                                                        | _                                                                      | "Provider<String>"                          | PropertyLocation.script
        "executable"                     | toSetter(property)      | "snyk_4"                                                        | _                                                                      | "String"                                    | PropertyLocation.script
        "executable"                     | toProviderSet(property) | "snyk_5"                                                        | _                                                                      | "Provider<String>"                          | PropertyLocation.script
        "executable"                     | toProviderSet(property) | "snyk_6"                                                        | _                                                                      | "String"                                    | PropertyLocation.script

        "snykPath"                       | _                       | _                                                               | null                                                                   | "Provider<Directory>"                       | PropertyLocation.none
        "snykPath"                       | _                       | "/path/to/snyk"                                                 | _                                                                      | _                                           | PropertyLocation.environment
        "snykPath"                       | _                       | "/path/to/snyk"                                                 | _                                                                      | _                                           | PropertyLocation.property
        "snykPath"                       | toSetter(property)      | "/path/to/snyk"                                                 | _                                                                      | "Provider<Directory>"                       | PropertyLocation.script
        "snykPath"                       | toSetter(property)      | "/path/to/snyk"                                                 | _                                                                      | "File"                                      | PropertyLocation.script
        "snykPath"                       | toProviderSet(property) | "/path/to/snyk"                                                 | _                                                                      | "Provider<Directory>"                       | PropertyLocation.script
        "snykPath"                       | toProviderSet(property) | "/path/to/snyk"                                                 | _                                                                      | "File"                                      | PropertyLocation.script

        "insecure"                       | _                       | _                                                               | false                                                                  | "Provider<Boolean>"                         | PropertyLocation.none
        "insecure"                       | _                       | "true"                                                          | true                                                                   | _                                           | PropertyLocation.environment
        "insecure"                       | _                       | "true"                                                          | true                                                                   | _                                           | PropertyLocation.property
        "insecure"                       | toSetter(property)      | true                                                            | _                                                                      | "Provider<Boolean>"                         | PropertyLocation.script
        "insecure"                       | toSetter(property)      | false                                                           | _                                                                      | "Boolean"                                   | PropertyLocation.script
        "insecure"                       | toProviderSet(property) | true                                                            | _                                                                      | "Provider<Boolean>"                         | PropertyLocation.script
        "insecure"                       | toProviderSet(property) | false                                                           | _                                                                      | "Boolean"                                   | PropertyLocation.script

        value = (type != _) ? wrapValueBasedOnType(rawValue, type.toString(), wrapValueFallback) : rawValue
        providedValue = (location == PropertyLocation.script) ? type : value
        testValue = (expectedValue == _) ? rawValue : expectedValue
        reason = location.reason() + ((location == PropertyLocation.none) ? "" : "  with '$providedValue' ")
        escapedValue = (value instanceof String) ? escapedPath(value) : value
        invocation = (method != _) ? "${method}(${escapedValue})" : "${property} = ${escapedValue}"
    }
}
