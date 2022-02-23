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

import com.wooga.gradle.test.ConventionSource
import com.wooga.gradle.test.GradleSpecUtils
import com.wooga.gradle.test.PropertyLocation
import com.wooga.gradle.test.PropertyQueryTaskWriter
import spock.lang.Unroll
import wooga.gradle.snyk.cli.*
import wooga.gradle.snyk.tasks.Monitor
import wooga.gradle.snyk.tasks.Report
import wooga.gradle.snyk.tasks.Test

import static com.wooga.gradle.test.PropertyUtils.*
import static com.wooga.gradle.test.SpecUtils.escapedPath

class SnykPluginIntegrationSpec extends SnykIntegrationSpec {

    def setup() {
        buildFile << """
        ${applyPlugin(SnykPlugin)}
        """
    }

    @Unroll
    def "sets convention for task type #taskType.simpleName and property #property"() {
        given: "write convention source assignment"
        conventionSource.write(buildFile, value.toString())

        and: "a task to query property from"

        buildFile << """
        class ${taskType.simpleName}Impl extends ${taskType.name} {
           //TODO this we need to adjust
           final String errorMessage = "Failed to create/update config section"
        }
        
        task ${subjectUnderTestName}(type: ${taskType.simpleName}Impl)
        """.stripIndent()

        when:
        def query = new PropertyQueryTaskWriter("${subjectUnderTestName}.${property}", invocation == _ ? ".getOrNull()" : invocation)
        query.write(buildFile)
        def result = runTasksSuccessfully(query.taskName)

        then:
        query.matches(result, testValue)

        where:
        taskType | property                         | rawValue                             | type                              | conventionSource                                                 | invocation || expectedValue
        Test     | "allProjects"                    | true                                 | "Boolean"                         | ConventionSource.extension(extensionName, property)              | _          || _
        Monitor  | "allProjects"                    | true                                 | "Boolean"                         | ConventionSource.extension(extensionName, property)              | _          || _
        Test     | "detectionDepth"                 | 22                                   | "Integer"                         | ConventionSource.extension(extensionName, property)              | _          || _
        Monitor  | "detectionDepth"                 | 45                                   | "Integer"                         | ConventionSource.extension(extensionName, property)              | _          || _
        Test     | "exclude"                        | [osPath('/path/exclude1')]           | "List<File>"                      | ConventionSource.extension(extensionName, property)              | _          || _
        Monitor  | "exclude"                        | [osPath('/path/exclude2')]           | "List<File>"                      | ConventionSource.extension(extensionName, property)              | _          || _
        Test     | "pruneRepeatedSubDependencies"   | true                                 | "Boolean"                         | ConventionSource.extension(extensionName, property)              | _          || _
        Monitor  | "pruneRepeatedSubDependencies"   | true                                 | "Boolean"                         | ConventionSource.extension(extensionName, property)              | _          || _
        Test     | "printDependencies"              | true                                 | "Boolean"                         | ConventionSource.extension(extensionName, property)              | _          || _
        Monitor  | "printDependencies"              | true                                 | "Boolean"                         | ConventionSource.extension(extensionName, property)              | _          || _
        Test     | "remoteRepoUrl"                  | "http://remote/url1"                 | "String"                          | ConventionSource.extension(extensionName, property)              | _          || _
        Monitor  | "remoteRepoUrl"                  | "http://remote/url2"                 | "String"                          | ConventionSource.extension(extensionName, property)              | _          || _
        Test     | "includeDevelopmentDependencies" | true                                 | "Boolean"                         | ConventionSource.extension(extensionName, property)              | _          || _
        Monitor  | "includeDevelopmentDependencies" | true                                 | "Boolean"                         | ConventionSource.extension(extensionName, property)              | _          || _
        Test     | "orgName"                        | "org1"                               | "String"                          | ConventionSource.extension(extensionName, property)              | _          || _
        Monitor  | "orgName"                        | "org1"                               | "String"                          | ConventionSource.extension(extensionName, property)              | _          || _
        Test     | "packageFile"                    | osPath("/path/to/package1")          | "RegularFile"                     | ConventionSource.extension(extensionName, property)              | _          || _
        Monitor  | "packageFile"                    | osPath("/path/to/package2")          | "RegularFile"                     | ConventionSource.extension(extensionName, property)              | _          || _
        Test     | "ignorePolicy"                   | true                                 | "Boolean"                         | ConventionSource.extension(extensionName, property)              | _          || _
        Monitor  | "ignorePolicy"                   | true                                 | "Boolean"                         | ConventionSource.extension(extensionName, property)              | _          || _
        Test     | "showVulnerablePaths"            | VulnerablePathsOption.some           | "VulnerablePathsOption"           | ConventionSource.extension(extensionName, property)              | _          || _
        Monitor  | "showVulnerablePaths"            | VulnerablePathsOption.all            | "VulnerablePathsOption"           | ConventionSource.extension(extensionName, property)              | _          || _
        Test     | "projectName"                    | "project1"                           | "String"                          | ConventionSource.extension(extensionName, property)              | _          || _
        Monitor  | "projectName"                    | "project1"                           | "String"                          | ConventionSource.extension(extensionName, property)              | _          || _
        Test     | "targetReference"                | "reference1"                         | "String"                          | ConventionSource.extension(extensionName, property)              | _          || _
        Monitor  | "targetReference"                | "reference1"                         | "String"                          | ConventionSource.extension(extensionName, property)              | _          || _
        Test     | "policyPath"                     | osPath("/path/to/policy1")           | "RegularFile"                     | ConventionSource.extension(extensionName, property)              | _          || _
        Monitor  | "policyPath"                     | osPath("/path/to/policy2")           | "RegularFile"                     | ConventionSource.extension(extensionName, property)              | _          || _
        Test     | "printJson"                      | true                                 | "Boolean"                         | ConventionSource.extension(extensionName, property)              | _          || _
        Monitor  | "printJson"                      | true                                 | "Boolean"                         | ConventionSource.extension(extensionName, property)              | _          || _
        Test     | "jsonOutputPath"                 | osPath("/path/to/json1")             | "RegularFile"                     | ConventionSource.extension(extensionName, property)              | _          || _
        Monitor  | "jsonOutputPath"                 | osPath("/path/to/json2")             | "RegularFile"                     | ConventionSource.extension(extensionName, property)              | _          || _
        Test     | "printSarif"                     | true                                 | "Boolean"                         | ConventionSource.extension(extensionName, property)              | _          || _
        Monitor  | "printSarif"                     | true                                 | "Boolean"                         | ConventionSource.extension(extensionName, property)              | _          || _
        Test     | "sarifOutputPath"                | osPath("/path/to/sarif1")            | "RegularFile"                     | ConventionSource.extension(extensionName, property)              | _          || _
        Monitor  | "sarifOutputPath"                | osPath("/path/to/sarif2")            | "RegularFile"                     | ConventionSource.extension(extensionName, property)              | _          || _
        Test     | "severityThreshold"              | SeverityThresholdOption.critical     | "SeverityThresholdOption"         | ConventionSource.extension(extensionName, property)              | _          || _
        Monitor  | "severityThreshold"              | SeverityThresholdOption.low          | "SeverityThresholdOption"         | ConventionSource.extension(extensionName, property)              | _          || _
        Test     | "failOn"                         | FailOnOption.all                     | "FailOnOption"                    | ConventionSource.extension(extensionName, property)              | _          || _
        Monitor  | "failOn"                         | FailOnOption.upgradable              | "FailOnOption"                    | ConventionSource.extension(extensionName, property)              | _          || _
        Test     | "compilerArguments"              | ['--flag1']                          | "List<String>"                    | ConventionSource.extension(extensionName, property)              | _          || _
        Monitor  | "compilerArguments"              | ['--flag2']                          | "List<String>"                    | ConventionSource.extension(extensionName, property)              | _          || _

        Test     | "assetsProjectName"              | true                                 | "Boolean"                         | ConventionSource.extension(extensionName, property)              | _          || _
        Monitor  | "assetsProjectName"              | true                                 | "Boolean"                         | ConventionSource.extension(extensionName, property)              | _          || _
        Test     | "packagesFolder"                 | osPath("/path/to/packages1")         | "Directory"                       | ConventionSource.extension(extensionName, property)              | _          || _
        Monitor  | "packagesFolder"                 | osPath("/path/to/packages2")         | "Directory"                       | ConventionSource.extension(extensionName, property)              | _          || _
        Test     | "projectNamePrefix"              | "prefix1"                            | "String"                          | ConventionSource.extension(extensionName, property)              | _          || _
        Monitor  | "projectNamePrefix"              | "prefix2"                            | "String"                          | ConventionSource.extension(extensionName, property)              | _          || _

        Test     | "subProject"                     | "project1"                           | "String"                          | ConventionSource.extension(extensionName, property)              | _          || _
        Monitor  | "subProject"                     | "project2"                           | "String"                          | ConventionSource.extension(extensionName, property)              | _          || _
        Test     | "allSubProjects"                 | true                                 | "Boolean"                         | ConventionSource.extension(extensionName, property)              | _          || _
        Monitor  | "allSubProjects"                 | true                                 | "Boolean"                         | ConventionSource.extension(extensionName, property)              | _          || _
        Test     | "configurationMatching"          | "config1"                            | "String"                          | ConventionSource.extension(extensionName, property)              | _          || _
        Monitor  | "configurationMatching"          | "config2"                            | "String"                          | ConventionSource.extension(extensionName, property)              | _          || _
        Test     | "configurationAttributes"        | ['attribute1']                       | "List<String>"                    | ConventionSource.extension(extensionName, property)              | _          || _
        Monitor  | "configurationAttributes"        | ['attribute2']                       | "List<String>"                    | ConventionSource.extension(extensionName, property)              | _          || _
        Test     | "reachable"                      | true                                 | "Boolean"                         | ConventionSource.extension(extensionName, property)              | _          || _
        Monitor  | "reachable"                      | true                                 | "Boolean"                         | ConventionSource.extension(extensionName, property)              | _          || _
        Test     | "reachableTimeout"               | 22                                   | "Integer"                         | ConventionSource.extension(extensionName, property)              | _          || _
        Monitor  | "reachableTimeout"               | 45                                   | "Integer"                         | ConventionSource.extension(extensionName, property)              | _          || _
        Test     | "initScript"                     | osPath("/path/to/initScript1")       | "RegularFile"                     | ConventionSource.extension(extensionName, property)              | _          || _
        Monitor  | "initScript"                     | osPath("/path/to/initScript2")       | "RegularFile"                     | ConventionSource.extension(extensionName, property)              | _          || _

        Test     | "scanAllUnmanaged"               | true                                 | "Boolean"                         | ConventionSource.extension(extensionName, property)              | _          || _
        Monitor  | "scanAllUnmanaged"               | true                                 | "Boolean"                         | ConventionSource.extension(extensionName, property)              | _          || _

        Test     | "strictOutOfSync"                | true                                 | "Boolean"                         | ConventionSource.extension(extensionName, property)              | _          || _
        Monitor  | "strictOutOfSync"                | true                                 | "Boolean"                         | ConventionSource.extension(extensionName, property)              | _          || _

        Test     | "command"                        | "command1"                           | "String"                          | ConventionSource.extension(extensionName, property)              | _          || _
        Monitor  | "command"                        | "command2"                           | "String"                          | ConventionSource.extension(extensionName, property)              | _          || _
        Test     | "skipUnresolved"                 | true                                 | "Boolean"                         | ConventionSource.extension(extensionName, property)              | _          || _
        Monitor  | "skipUnresolved"                 | true                                 | "Boolean"                         | ConventionSource.extension(extensionName, property)              | _          || _

        Test     | "insecure"                       | true                                 | "Boolean"                         | ConventionSource.extension(extensionName, property)              | _          || _
        Monitor  | "insecure"                       | true                                 | "Boolean"                         | ConventionSource.extension(extensionName, property)              | _          || _
        Test     | "debug"                          | true                                 | "Boolean"                         | ConventionSource.extension(extensionName, property)              | _          || _
        Monitor  | "debug"                          | true                                 | "Boolean"                         | ConventionSource.extension(extensionName, property)              | _          || _

        Test     | "token"                          | "test_token_1"                       | "String"                          | ConventionSource.extension(extensionName, property)              | _          || _
        Monitor  | "token"                          | "test_token_2"                       | "String"                          | ConventionSource.extension(extensionName, property)              | _          || _

        Test     | "executableName"                 | "snyk1"                              | "String"                          | ConventionSource.extension(extensionName, property)              | _          || _
        Monitor  | "executableName"                 | "snyk2"                              | "String"                          | ConventionSource.extension(extensionName, property)              | _          || _
        Test     | "snykPath"                       | osPath("/path/to/snyk_home_1")       | "Directory"                       | ConventionSource.extension(extensionName, property)              | _          || _
        Monitor  | "snykPath"                       | osPath("/path/to/snyk_home_2")       | "Directory"                       | ConventionSource.extension(extensionName, property)              | _          || _

        Monitor  | "trustPolicies"                  | true                                 | "Boolean"                         | ConventionSource.extension(extensionName, property)              | _          || _
        Monitor  | "projectEnvironment"             | [EnvironmentOption.onprem]           | "List<EnvironmentOption>"         | ConventionSource.extension(extensionName, property)              | _          || _
        Monitor  | "projectLifecycle"               | [LifecycleOption.development]        | "List<LifecycleOption>"           | ConventionSource.extension(extensionName, property)              | _          || _
        Monitor  | "projectBusinessCriticality"     | [BusinessCriticalityOption.critical] | "List<BusinessCriticalityOption>" | ConventionSource.extension(extensionName, property)              | _          || _
        Monitor  | "projectTags"                    | ["foo": "bar"]                       | "Map<String, String>"             | ConventionSource.extension(extensionName, property)              | _          || _

        Test     | "reports.sarif.enabled"          | true                                 | "Boolean"                         | ConventionSource.extension(extensionName, "sarifReportsEnabled") | ""         || _
        Test     | "reports.json.enabled"           | true                                 | "Boolean"                         | ConventionSource.extension(extensionName, "jsonReportsEnabled")  | ""         || _
        Report   | "reports.sarif.enabled"          | true                                 | "Boolean"                         | ConventionSource.extension(extensionName, "sarifReportsEnabled") | ""         || _
        Report   | "reports.json.enabled"           | false                                | "Boolean"                         | ConventionSource.extension(extensionName, "jsonReportsEnabled")  | ""         || _

        value = (type != _) ? wrapValueBasedOnType(rawValue, type.toString(), wrapValueFallback) : rawValue
        testValue = (expectedValue == _) ? rawValue : expectedValue
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

        "exclude"                        | _                       | _                                                               | null                                                                   | "Provider<List<File>>"                      | PropertyLocation.none
        "exclude"                        | _                       | [osPath('/path/one'), osPath('/path/two')].join(',')            | [osPath('/path/one'), osPath('/path/two')]                             | _                                           | PropertyLocation.environment
        "exclude"                        | _                       | [osPath('/path/one'), osPath('/path/two')].join(',')            | [osPath('/path/one'), osPath('/path/two')]                             | _                                           | PropertyLocation.property
        "exclude"                        | toSetter(property)      | [osPath('/path/one'), osPath('/path/two')]                      | _                                                                      | "Provider<List<File>>"                      | PropertyLocation.script
        "exclude"                        | toSetter(property)      | [osPath('/path/one'), osPath('/path/two')]                      | _                                                                      | "List<File>"                                | PropertyLocation.script
        "exclude"                        | toProviderSet(property) | [osPath('/path/one'), osPath('/path/two')]                      | _                                                                      | "Provider<List<File>>"                      | PropertyLocation.script
        "exclude"                        | toProviderSet(property) | [osPath('/path/one'), osPath('/path/two')]                      | _                                                                      | "List<File>"                                | PropertyLocation.script

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
        "packageFile"                    | _                       | osPath("/path/to/package1")                                     | _                                                                      | _                                           | PropertyLocation.environment
        "packageFile"                    | _                       | osPath("/path/to/package2")                                     | _                                                                      | _                                           | PropertyLocation.property
        "packageFile"                    | toSetter(property)      | osPath("/path/to/package3")                                     | _                                                                      | "Provider<RegularFile>"                     | PropertyLocation.script
        "packageFile"                    | toSetter(property)      | osPath("/path/to/package4")                                     | _                                                                      | "File"                                      | PropertyLocation.script
        "packageFile"                    | toProviderSet(property) | osPath("/path/to/package5")                                     | _                                                                      | "Provider<RegularFile>"                     | PropertyLocation.script
        "packageFile"                    | toProviderSet(property) | osPath("/path/to/package6")                                     | _                                                                      | "File"                                      | PropertyLocation.script

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
        "policyPath"                     | _                       | osPath("/path/to/policy1")                                      | _                                                                      | _                                           | PropertyLocation.environment
        "policyPath"                     | _                       | osPath("/path/to/policy2")                                      | _                                                                      | _                                           | PropertyLocation.property
        "policyPath"                     | toSetter(property)      | osPath("/path/to/policy3")                                      | _                                                                      | "Provider<RegularFile>"                     | PropertyLocation.script
        "policyPath"                     | toSetter(property)      | osPath("/path/to/policy4")                                      | _                                                                      | "File"                                      | PropertyLocation.script
        "policyPath"                     | toProviderSet(property) | osPath("/path/to/policy5")                                      | _                                                                      | "Provider<RegularFile>"                     | PropertyLocation.script
        "policyPath"                     | toProviderSet(property) | osPath("/path/to/policy6")                                      | _                                                                      | "File"                                      | PropertyLocation.script

        "printJson"                      | _                       | _                                                               | false                                                                  | "Provider<Boolean>"                         | PropertyLocation.none
        "printJson"                      | _                       | "true"                                                          | true                                                                   | _                                           | PropertyLocation.environment
        "printJson"                      | _                       | "true"                                                          | true                                                                   | _                                           | PropertyLocation.property
        "printJson"                      | toSetter(property)      | true                                                            | _                                                                      | "Provider<Boolean>"                         | PropertyLocation.script
        "printJson"                      | toSetter(property)      | false                                                           | _                                                                      | "Boolean"                                   | PropertyLocation.script
        "printJson"                      | toProviderSet(property) | true                                                            | _                                                                      | "Provider<Boolean>"                         | PropertyLocation.script
        "printJson"                      | toProviderSet(property) | false                                                           | _                                                                      | "Boolean"                                   | PropertyLocation.script

        "jsonOutputPath"                 | _                       | _                                                               | null                                                                   | "Provider<RegularFile>"                     | PropertyLocation.none
        "jsonOutputPath"                 | _                       | osPath("/path/to/json1")                                        | _                                                                      | _                                           | PropertyLocation.environment
        "jsonOutputPath"                 | _                       | osPath("/path/to/json2")                                        | _                                                                      | _                                           | PropertyLocation.property
        "jsonOutputPath"                 | toSetter(property)      | osPath("/path/to/json3")                                        | _                                                                      | "Provider<RegularFile>"                     | PropertyLocation.script
        "jsonOutputPath"                 | toSetter(property)      | osPath("/path/to/json4")                                        | _                                                                      | "File"                                      | PropertyLocation.script
        "jsonOutputPath"                 | toProviderSet(property) | osPath("/path/to/json5")                                        | _                                                                      | "Provider<RegularFile>"                     | PropertyLocation.script
        "jsonOutputPath"                 | toProviderSet(property) | osPath("/path/to/json6")                                        | _                                                                      | "File"                                      | PropertyLocation.script

        "printSarif"                     | _                       | _                                                               | false                                                                  | "Provider<Boolean>"                         | PropertyLocation.none
        "printSarif"                     | _                       | "true"                                                          | true                                                                   | _                                           | PropertyLocation.environment
        "printSarif"                     | _                       | "true"                                                          | true                                                                   | _                                           | PropertyLocation.property
        "printSarif"                     | toSetter(property)      | true                                                            | _                                                                      | "Provider<Boolean>"                         | PropertyLocation.script
        "printSarif"                     | toSetter(property)      | false                                                           | _                                                                      | "Boolean"                                   | PropertyLocation.script
        "printSarif"                     | toProviderSet(property) | true                                                            | _                                                                      | "Provider<Boolean>"                         | PropertyLocation.script
        "printSarif"                     | toProviderSet(property) | false                                                           | _                                                                      | "Boolean"                                   | PropertyLocation.script

        "sarifOutputPath"                | _                       | _                                                               | null                                                                   | "Provider<RegularFile>"                     | PropertyLocation.none
        "sarifOutputPath"                | _                       | osPath("/path/to/sarif1")                                       | _                                                                      | _                                           | PropertyLocation.environment
        "sarifOutputPath"                | _                       | osPath("/path/to/sarif2")                                       | _                                                                      | _                                           | PropertyLocation.property
        "sarifOutputPath"                | toSetter(property)      | osPath("/path/to/sarif3")                                       | _                                                                      | "Provider<RegularFile>"                     | PropertyLocation.script
        "sarifOutputPath"                | toSetter(property)      | osPath("/path/to/sarif4")                                       | _                                                                      | "File"                                      | PropertyLocation.script
        "sarifOutputPath"                | toProviderSet(property) | osPath("/path/to/sarif5")                                       | _                                                                      | "Provider<RegularFile>"                     | PropertyLocation.script
        "sarifOutputPath"                | toProviderSet(property) | osPath("/path/to/sarif6")                                       | _                                                                      | "File"                                      | PropertyLocation.script

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

        "assetsProjectName"              | _                       | _                                                               | false                                                                  | "Provider<Boolean>"                         | PropertyLocation.none
        "assetsProjectName"              | _                       | "true"                                                          | true                                                                   | _                                           | PropertyLocation.environment
        "assetsProjectName"              | _                       | "true"                                                          | true                                                                   | _                                           | PropertyLocation.property
        "assetsProjectName"              | toSetter(property)      | true                                                            | _                                                                      | "Provider<Boolean>"                         | PropertyLocation.script
        "assetsProjectName"              | toSetter(property)      | false                                                           | _                                                                      | "Boolean"                                   | PropertyLocation.script
        "assetsProjectName"              | toProviderSet(property) | true                                                            | _                                                                      | "Provider<Boolean>"                         | PropertyLocation.script
        "assetsProjectName"              | toProviderSet(property) | false                                                           | _                                                                      | "Boolean"                                   | PropertyLocation.script

        "packagesFolder"                 | _                       | _                                                               | null                                                                   | "Provider<Directory>"                       | PropertyLocation.none
        "packagesFolder"                 | _                       | osPath("/path/to/sarif1")                                       | _                                                                      | _                                           | PropertyLocation.environment
        "packagesFolder"                 | _                       | osPath("/path/to/sarif2")                                       | _                                                                      | _                                           | PropertyLocation.property
        "packagesFolder"                 | toSetter(property)      | osPath("/path/to/sarif3")                                       | _                                                                      | "Provider<Directory>"                       | PropertyLocation.script
        "packagesFolder"                 | toSetter(property)      | osPath("/path/to/sarif4")                                       | _                                                                      | "File"                                      | PropertyLocation.script
        "packagesFolder"                 | toProviderSet(property) | osPath("/path/to/sarif5")                                       | _                                                                      | "Provider<Directory>"                       | PropertyLocation.script
        "packagesFolder"                 | toProviderSet(property) | osPath("/path/to/sarif6")                                       | _                                                                      | "File"                                      | PropertyLocation.script

        "projectNamePrefix"              | _                       | _                                                               | null                                                                   | "Provider<String>"                          | PropertyLocation.none
        "projectNamePrefix"              | _                       | "prefix_name1"                                                  | _                                                                      | _                                           | PropertyLocation.environment
        "projectNamePrefix"              | _                       | "prefix_name2"                                                  | _                                                                      | _                                           | PropertyLocation.property
        "projectNamePrefix"              | toSetter(property)      | "prefix_name3"                                                  | _                                                                      | "Provider<String>"                          | PropertyLocation.script
        "projectNamePrefix"              | toSetter(property)      | "prefix_name4"                                                  | _                                                                      | "String"                                    | PropertyLocation.script
        "projectNamePrefix"              | toProviderSet(property) | "prefix_name5"                                                  | _                                                                      | "Provider<String>"                          | PropertyLocation.script
        "projectNamePrefix"              | toProviderSet(property) | "prefix_name6"                                                  | _                                                                      | "String"                                    | PropertyLocation.script

        "strictOutOfSync"                | _                       | _                                                               | false                                                                  | "Provider<Boolean>"                         | PropertyLocation.none
        "strictOutOfSync"                | _                       | "true"                                                          | true                                                                   | _                                           | PropertyLocation.environment
        "strictOutOfSync"                | _                       | "true"                                                          | true                                                                   | _                                           | PropertyLocation.property
        "strictOutOfSync"                | toSetter(property)      | true                                                            | _                                                                      | "Provider<Boolean>"                         | PropertyLocation.script
        "strictOutOfSync"                | toSetter(property)      | false                                                           | _                                                                      | "Boolean"                                   | PropertyLocation.script
        "strictOutOfSync"                | toProviderSet(property) | true                                                            | _                                                                      | "Provider<Boolean>"                         | PropertyLocation.script
        "strictOutOfSync"                | toProviderSet(property) | false                                                           | _                                                                      | "Boolean"                                   | PropertyLocation.script

        "insecure"                       | _                       | _                                                               | false                                                                  | "Provider<Boolean>"                         | PropertyLocation.none
        "insecure"                       | _                       | "true"                                                          | true                                                                   | _                                           | PropertyLocation.environment
        "insecure"                       | _                       | "true"                                                          | true                                                                   | _                                           | PropertyLocation.property
        "insecure"                       | toSetter(property)      | true                                                            | _                                                                      | "Provider<Boolean>"                         | PropertyLocation.script
        "insecure"                       | toSetter(property)      | false                                                           | _                                                                      | "Boolean"                                   | PropertyLocation.script
        "insecure"                       | toProviderSet(property) | true                                                            | _                                                                      | "Provider<Boolean>"                         | PropertyLocation.script
        "insecure"                       | toProviderSet(property) | false                                                           | _                                                                      | "Boolean"                                   | PropertyLocation.script

        "scanAllUnmanaged"               | _                       | _                                                               | false                                                                  | "Provider<Boolean>"                         | PropertyLocation.none
        "scanAllUnmanaged"               | _                       | "true"                                                          | true                                                                   | _                                           | PropertyLocation.environment
        "scanAllUnmanaged"               | _                       | "true"                                                          | true                                                                   | _                                           | PropertyLocation.property
        "scanAllUnmanaged"               | toSetter(property)      | true                                                            | _                                                                      | "Provider<Boolean>"                         | PropertyLocation.script
        "scanAllUnmanaged"               | toSetter(property)      | false                                                           | _                                                                      | "Boolean"                                   | PropertyLocation.script
        "scanAllUnmanaged"               | toProviderSet(property) | true                                                            | _                                                                      | "Provider<Boolean>"                         | PropertyLocation.script
        "scanAllUnmanaged"               | toProviderSet(property) | false                                                           | _                                                                      | "Boolean"                                   | PropertyLocation.script

        "reachable"                      | _                       | _                                                               | false                                                                  | "Provider<Boolean>"                         | PropertyLocation.none
        "reachable"                      | _                       | "true"                                                          | true                                                                   | _                                           | PropertyLocation.environment
        "reachable"                      | _                       | "true"                                                          | true                                                                   | _                                           | PropertyLocation.property
        "reachable"                      | toSetter(property)      | true                                                            | _                                                                      | "Provider<Boolean>"                         | PropertyLocation.script
        "reachable"                      | toSetter(property)      | false                                                           | _                                                                      | "Boolean"                                   | PropertyLocation.script
        "reachable"                      | toProviderSet(property) | true                                                            | _                                                                      | "Provider<Boolean>"                         | PropertyLocation.script
        "reachable"                      | toProviderSet(property) | false                                                           | _                                                                      | "Boolean"                                   | PropertyLocation.script

        "reachableTimeout"               | _                       | _                                                               | null                                                                   | "Provider<Integer>"                         | PropertyLocation.none
        "reachableTimeout"               | _                       | 1                                                               | _                                                                      | _                                           | PropertyLocation.environment
        "reachableTimeout"               | _                       | 2                                                               | _                                                                      | _                                           | PropertyLocation.property
        "reachableTimeout"               | toSetter(property)      | 3                                                               | _                                                                      | "Provider<Integer>"                         | PropertyLocation.script
        "reachableTimeout"               | toSetter(property)      | 4                                                               | _                                                                      | "Integer"                                   | PropertyLocation.script
        "reachableTimeout"               | toProviderSet(property) | 5                                                               | _                                                                      | "Provider<Integer>"                         | PropertyLocation.script
        "reachableTimeout"               | toProviderSet(property) | 6                                                               | _                                                                      | "Integer"                                   | PropertyLocation.script

        "subProject"                     | _                       | _                                                               | null                                                                   | "Provider<String>"                          | PropertyLocation.none
        "subProject"                     | _                       | "subproject1"                                                   | _                                                                      | _                                           | PropertyLocation.environment
        "subProject"                     | _                       | "subproject2"                                                   | _                                                                      | _                                           | PropertyLocation.property
        "subProject"                     | toSetter(property)      | "subproject3"                                                   | _                                                                      | "Provider<String>"                          | PropertyLocation.script
        "subProject"                     | toSetter(property)      | "subproject4"                                                   | _                                                                      | "String"                                    | PropertyLocation.script
        "subProject"                     | toProviderSet(property) | "subproject5"                                                   | _                                                                      | "Provider<String>"                          | PropertyLocation.script
        "subProject"                     | toProviderSet(property) | "subproject6"                                                   | _                                                                      | "String"                                    | PropertyLocation.script

        "allSubProjects"                 | _                       | _                                                               | false                                                                  | "Provider<Boolean>"                         | PropertyLocation.none
        "allSubProjects"                 | _                       | "true"                                                          | true                                                                   | _                                           | PropertyLocation.environment
        "allSubProjects"                 | _                       | "true"                                                          | true                                                                   | _                                           | PropertyLocation.property
        "allSubProjects"                 | toSetter(property)      | true                                                            | _                                                                      | "Provider<Boolean>"                         | PropertyLocation.script
        "allSubProjects"                 | toSetter(property)      | false                                                           | _                                                                      | "Boolean"                                   | PropertyLocation.script
        "allSubProjects"                 | toProviderSet(property) | true                                                            | _                                                                      | "Provider<Boolean>"                         | PropertyLocation.script
        "allSubProjects"                 | toProviderSet(property) | false                                                           | _                                                                      | "Boolean"                                   | PropertyLocation.script

        "configurationMatching"          | _                       | _                                                               | null                                                                   | "Provider<String>"                          | PropertyLocation.none
        "configurationMatching"          | _                       | "config_1"                                                      | _                                                                      | _                                           | PropertyLocation.environment
        "configurationMatching"          | _                       | "config_2"                                                      | _                                                                      | _                                           | PropertyLocation.property
        "configurationMatching"          | toSetter(property)      | "config_3"                                                      | _                                                                      | "Provider<String>"                          | PropertyLocation.script
        "configurationMatching"          | toSetter(property)      | "config_4"                                                      | _                                                                      | "String"                                    | PropertyLocation.script
        "configurationMatching"          | toProviderSet(property) | "config_5"                                                      | _                                                                      | "Provider<String>"                          | PropertyLocation.script
        "configurationMatching"          | toProviderSet(property) | "config_6"                                                      | _                                                                      | "String"                                    | PropertyLocation.script

        "configurationAttributes"        | _                       | _                                                               | null                                                                   | "Provider<List<String>>"                    | PropertyLocation.none
        "configurationAttributes"        | _                       | 'config1,config2'                                               | ['config1', 'config2']                                                 | _                                           | PropertyLocation.environment
        "configurationAttributes"        | _                       | 'config1,config2'                                               | ['config1', 'config2']                                                 | _                                           | PropertyLocation.property
        "configurationAttributes"        | toSetter(property)      | ["config1", "config2"]                                          | _                                                                      | "Provider<List<String>>"                    | PropertyLocation.script
        "configurationAttributes"        | toSetter(property)      | ["config1", "config2"]                                          | _                                                                      | "List<String>"                              | PropertyLocation.script
        "configurationAttributes"        | toProviderSet(property) | ["config1", "config2"]                                          | _                                                                      | "Provider<List<String>>"                    | PropertyLocation.script
        "configurationAttributes"        | toProviderSet(property) | ["config1", "config2"]                                          | _                                                                      | "List<String>"                              | PropertyLocation.script

        "command"                        | _                       | _                                                               | null                                                                   | "Provider<String>"                          | PropertyLocation.none
        "command"                        | _                       | "command_1"                                                     | _                                                                      | _                                           | PropertyLocation.environment
        "command"                        | _                       | "command_2"                                                     | _                                                                      | _                                           | PropertyLocation.property
        "command"                        | toSetter(property)      | "command_3"                                                     | _                                                                      | "Provider<String>"                          | PropertyLocation.script
        "command"                        | toSetter(property)      | "command_4"                                                     | _                                                                      | "String"                                    | PropertyLocation.script
        "command"                        | toProviderSet(property) | "command_5"                                                     | _                                                                      | "Provider<String>"                          | PropertyLocation.script
        "command"                        | toProviderSet(property) | "command_6"                                                     | _                                                                      | "String"                                    | PropertyLocation.script

        "initScript"                     | _                       | _                                                               | null                                                                   | "Provider<RegularFile>"                     | PropertyLocation.none
        "initScript"                     | _                       | osPath("/path/to/initScript_1")                                 | _                                                                      | _                                           | PropertyLocation.environment
        "initScript"                     | _                       | osPath("/path/to/initScript_2")                                 | _                                                                      | _                                           | PropertyLocation.property
        "initScript"                     | toSetter(property)      | osPath("/path/to/initScript_3")                                 | _                                                                      | "Provider<RegularFile>"                     | PropertyLocation.script
        "initScript"                     | toSetter(property)      | osPath("/path/to/initScript_4")                                 | _                                                                      | "File"                                      | PropertyLocation.script
        "initScript"                     | toProviderSet(property) | osPath("/path/to/initScript_5")                                 | _                                                                      | "Provider<RegularFile>"                     | PropertyLocation.script
        "initScript"                     | toProviderSet(property) | osPath("/path/to/initScript_6")                                 | _                                                                      | "File"                                      | PropertyLocation.script

        "skipUnresolved"                 | _                       | _                                                               | false                                                                  | "Provider<Boolean>"                         | PropertyLocation.none
        "skipUnresolved"                 | _                       | "true"                                                          | true                                                                   | _                                           | PropertyLocation.environment
        "skipUnresolved"                 | _                       | "true"                                                          | true                                                                   | _                                           | PropertyLocation.property
        "skipUnresolved"                 | toSetter(property)      | true                                                            | _                                                                      | "Provider<Boolean>"                         | PropertyLocation.script
        "skipUnresolved"                 | toSetter(property)      | false                                                           | _                                                                      | "Boolean"                                   | PropertyLocation.script
        "skipUnresolved"                 | toProviderSet(property) | true                                                            | _                                                                      | "Provider<Boolean>"                         | PropertyLocation.script
        "skipUnresolved"                 | toProviderSet(property) | false                                                           | _                                                                      | "Boolean"                                   | PropertyLocation.script

        "yarnWorkspaces"                 | _                       | _                                                               | false                                                                  | "Provider<Boolean>"                         | PropertyLocation.none
        "yarnWorkspaces"                 | _                       | "true"                                                          | true                                                                   | _                                           | PropertyLocation.environment
        "yarnWorkspaces"                 | _                       | "true"                                                          | true                                                                   | _                                           | PropertyLocation.property
        "yarnWorkspaces"                 | toSetter(property)      | true                                                            | _                                                                      | "Provider<Boolean>"                         | PropertyLocation.script
        "yarnWorkspaces"                 | toSetter(property)      | false                                                           | _                                                                      | "Boolean"                                   | PropertyLocation.script
        "yarnWorkspaces"                 | toProviderSet(property) | true                                                            | _                                                                      | "Provider<Boolean>"                         | PropertyLocation.script
        "yarnWorkspaces"                 | toProviderSet(property) | false                                                           | _                                                                      | "Boolean"                                   | PropertyLocation.script

        // Not correct since the token is set by convention fron environment
        //"token"                          | _                       | _                                                               | null                                                                   | "Provider<String>"                          | PropertyLocation.none
        "token"                          | _                       | "test_token1"                                                   | _                                                                      | _                                           | PropertyLocation.environment
        "token"                          | _                       | "test_token2"                                                   | _                                                                      | _                                           | PropertyLocation.property
        "token"                          | toSetter(property)      | "test_token3"                                                   | _                                                                      | "Provider<String>"                          | PropertyLocation.script
        "token"                          | toSetter(property)      | "test_token4"                                                   | _                                                                      | "String"                                    | PropertyLocation.script
        "token"                          | toProviderSet(property) | "test_token5"                                                   | _                                                                      | "Provider<String>"                          | PropertyLocation.script
        "token"                          | toProviderSet(property) | "test_token6"                                                   | _                                                                      | "String"                                    | PropertyLocation.script

        "executableName"                 | _                       | "snyk"                                                          | _                                                                      | "Provider<String>"                          | PropertyLocation.none
        "executableName"                 | _                       | "snyk_1"                                                        | _                                                                      | _                                           | PropertyLocation.environment
        "executableName"                 | _                       | "snyk_2"                                                        | _                                                                      | _                                           | PropertyLocation.property
        "executableName"                 | toSetter(property)      | "snyk_3"                                                        | _                                                                      | "Provider<String>"                          | PropertyLocation.script
        "executableName"                 | toSetter(property)      | "snyk_4"                                                        | _                                                                      | "String"                                    | PropertyLocation.script
        "executableName"                 | toProviderSet(property) | "snyk_5"                                                        | _                                                                      | "Provider<String>"                          | PropertyLocation.script
        "executableName"                 | toProviderSet(property) | "snyk_6"                                                        | _                                                                      | "String"                                    | PropertyLocation.script

        "version"                        | _                       | _                                                               | "v1.840.0"                                                             | "Provider<String>"                          | PropertyLocation.none
        "version"                        | _                       | "v1.0"                                                          | _                                                                      | _                                           | PropertyLocation.environment
        "version"                        | _                       | "v1.1"                                                          | _                                                                      | _                                           | PropertyLocation.property
        "version"                        | toSetter(property)      | "v1.12"                                                         | _                                                                      | "Provider<String>"                          | PropertyLocation.script
        "version"                        | toSetter(property)      | "v1.123"                                                        | _                                                                      | "String"                                    | PropertyLocation.script
        "version"                        | toProviderSet(property) | "v2.0"                                                          | _                                                                      | "Provider<String>"                          | PropertyLocation.script
        "version"                        | toProviderSet(property) | "v2.00"                                                         | _                                                                      | "String"                                    | PropertyLocation.script


        "snykPath"                       | _                       | _                                                               | null                                                                   | "Provider<Directory>"                       | PropertyLocation.none
        "snykPath"                       | _                       | osPath("/path/to/snyk")                                         | _                                                                      | _                                           | PropertyLocation.environment
        "snykPath"                       | _                       | osPath("/path/to/snyk")                                         | _                                                                      | _                                           | PropertyLocation.property
        "snykPath"                       | toSetter(property)      | osPath("/path/to/snyk")                                         | _                                                                      | "Provider<Directory>"                       | PropertyLocation.script
        "snykPath"                       | toSetter(property)      | osPath("/path/to/snyk")                                         | _                                                                      | "File"                                      | PropertyLocation.script
        "snykPath"                       | toProviderSet(property) | osPath("/path/to/snyk")                                         | _                                                                      | "Provider<Directory>"                       | PropertyLocation.script
        "snykPath"                       | toProviderSet(property) | osPath("/path/to/snyk")                                         | _                                                                      | "File"                                      | PropertyLocation.script

        "insecure"                       | _                       | _                                                               | false                                                                  | "Provider<Boolean>"                         | PropertyLocation.none
        "insecure"                       | _                       | "true"                                                          | true                                                                   | _                                           | PropertyLocation.environment
        "insecure"                       | _                       | "true"                                                          | true                                                                   | _                                           | PropertyLocation.property
        "insecure"                       | toSetter(property)      | true                                                            | _                                                                      | "Provider<Boolean>"                         | PropertyLocation.script
        "insecure"                       | toSetter(property)      | false                                                           | _                                                                      | "Boolean"                                   | PropertyLocation.script
        "insecure"                       | toProviderSet(property) | true                                                            | _                                                                      | "Provider<Boolean>"                         | PropertyLocation.script
        "insecure"                       | toProviderSet(property) | false                                                           | _                                                                      | "Boolean"                                   | PropertyLocation.script

        "sarifReportsEnabled"            | _                       | _                                                               | false                                                                  | "Provider<Boolean>"                         | PropertyLocation.none
        "sarifReportsEnabled"            | _                       | "true"                                                          | true                                                                   | _                                           | PropertyLocation.environment
        "sarifReportsEnabled"            | _                       | "true"                                                          | true                                                                   | _                                           | PropertyLocation.property
        "sarifReportsEnabled"            | toSetter(property)      | true                                                            | _                                                                      | "Provider<Boolean>"                         | PropertyLocation.script
        "sarifReportsEnabled"            | toSetter(property)      | false                                                           | _                                                                      | "Boolean"                                   | PropertyLocation.script
        "sarifReportsEnabled"            | toProviderSet(property) | true                                                            | _                                                                      | "Provider<Boolean>"                         | PropertyLocation.script
        "sarifReportsEnabled"            | toProviderSet(property) | false                                                           | _                                                                      | "Boolean"                                   | PropertyLocation.script

        "jsonReportsEnabled"             | _                       | _                                                               | true                                                                   | "Provider<Boolean>"                         | PropertyLocation.none
        "jsonReportsEnabled"             | _                       | "true"                                                          | true                                                                   | _                                           | PropertyLocation.environment
        "jsonReportsEnabled"             | _                       | "true"                                                          | true                                                                   | _                                           | PropertyLocation.property
        "jsonReportsEnabled"             | toSetter(property)      | true                                                            | _                                                                      | "Provider<Boolean>"                         | PropertyLocation.script
        "jsonReportsEnabled"             | toSetter(property)      | false                                                           | _                                                                      | "Boolean"                                   | PropertyLocation.script
        "jsonReportsEnabled"             | toProviderSet(property) | true                                                            | _                                                                      | "Provider<Boolean>"                         | PropertyLocation.script
        "jsonReportsEnabled"             | toProviderSet(property) | false                                                           | _                                                                      | "Boolean"                                   | PropertyLocation.script

        value = (type != _) ? wrapValueBasedOnType(rawValue, type.toString(), wrapValueFallback) : rawValue
        providedValue = (location == PropertyLocation.script) ? type : value
        testValue = (expectedValue == _) ? rawValue : expectedValue
        reason = location.reason() + ((location == PropertyLocation.none) ? "" : "  with '$providedValue' ")
        escapedValue = (value instanceof String) ? escapedPath(value) : value
        invocation = (method != _) ? "${method}(${escapedValue})" : "${property} = ${escapedValue}"
    }


    @Unroll("task #dependency runs before SnykTask")
    def "task #dependency runs before SnykTask"() {
        given: "a generic token in order for snykTask to run"
        def snykTaskName = "snykTaskTest"
        buildFile << """
        project.tasks.register("$snykTaskName", ${Test.name}) {
            token="anytoken"
        }
        """

        and: "auto install enabled"
        buildFile << """
        snyk.autoDownload = true
        """

        when:
        def result = runTasks(snykTaskName)

        then: "dependency task should run and run before the SnykTask"
        def executedTasks = GradleSpecUtils.executedTasks(result.standardOutput)
        def subjectTaskIndex = executedTasks.findIndexOf { it == ":${snykTaskName}" }
        def dependencyIndex = executedTasks.findIndexOf { it == ":${dependency}" }
        dependencyIndex > -1
        subjectTaskIndex > dependencyIndex

        where:
        dependency    | _
        "snykInstall" | _
    }


}
