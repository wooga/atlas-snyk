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

import static com.wooga.gradle.test.PropertyUtils.toProviderSet
import static com.wooga.gradle.test.PropertyUtils.toSetter
import static wooga.gradle.snyk.cli.SnykCLIOptions.*

class MonitorIntegrationSpec extends SnykCheckBaseIntegrationSpec<Monitor> {

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
        property                     | cliOption                        | rawValue                                                               | returnValue | type
        "trustPolicies"              | "--trust-policies"               | _                                                                      | true        | "Boolean"
        "projectEnvironment"         | "--project-environment"          | [EnvironmentOption.backend, EnvironmentOption.frontend]                | _           | "CLIList"
        "projectLifecycle"           | "--project-lifecycle"            | [LifecycleOption.production, LifecycleOption.development]              | _           | "CLIList"
        "projectBusinessCriticality" | "--project-business-criticality" | [BusinessCriticalityOption.critical, BusinessCriticalityOption.medium] | _           | "CLIList"
        "projectTags"                | "--project-tags"                 | ["foo": "test", "bar": "baz"]                                          | _           | "CLIMap"
        "projectTags"                | "--tags"                         | ["foo": "test", "bar": "baz"]                                          | _           | "CLIMap"

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
        property                     | method                  | rawValue                                                  | returnValue                      | type
        "trustPolicies"              | toProviderSet(property) | true                                                      | _                                | "Boolean"
        "trustPolicies"              | toProviderSet(property) | false                                                     | _                                | "Provider<Boolean>"
        "trustPolicies"              | toSetter(property)      | true                                                      | _                                | "Boolean"
        "trustPolicies"              | toSetter(property)      | false                                                     | _                                | "Provider<Boolean>"

        "projectEnvironment"         | toProviderSet(property) | [EnvironmentOption.backend]                               | _                                | "List<EnvironmentOption>"
        "projectEnvironment"         | toProviderSet(property) | [EnvironmentOption.backend, EnvironmentOption.frontend]   | _                                | "Provider<List<EnvironmentOption>>"
        "projectEnvironment"         | toSetter(property)      | [EnvironmentOption.distributed, EnvironmentOption.onprem] | _                                | "List<EnvironmentOption>"
        "projectEnvironment"         | toSetter(property)      | [EnvironmentOption.internal, EnvironmentOption.hosted]    | _                                | "Provider<List<EnvironmentOption>>"
        "projectEnvironment"         | property                | [EnvironmentOption.mobile, EnvironmentOption.external]    | _                                | "List<EnvironmentOption>"
        "projectEnvironment"         | property                | EnvironmentOption.internal                                | [EnvironmentOption.internal]     | "EnvironmentOption"
        "projectEnvironment"         | property                | [EnvironmentOption.backend, EnvironmentOption.frontend]   | _                                | "EnvironmentOption..."

        "projectLifecycle"           | toProviderSet(property) | [LifecycleOption.production, LifecycleOption.development] | _                                | "List<LifecycleOption>"
        "projectLifecycle"           | toProviderSet(property) | [LifecycleOption.sandbox, LifecycleOption.development]    | _                                | "Provider<List<LifecycleOption>>"
        "projectLifecycle"           | toSetter(property)      | [LifecycleOption.production, LifecycleOption.sandbox]     | _                                | "List<LifecycleOption>"
        "projectLifecycle"           | toSetter(property)      | [LifecycleOption.production, LifecycleOption.development] | _                                | "Provider<List<LifecycleOption>>"
        "projectLifecycle"           | property                | [LifecycleOption.production, LifecycleOption.development] | _                                | "List<LifecycleOption>"
        "projectLifecycle"           | property                | LifecycleOption.sandbox                                   | [LifecycleOption.sandbox]        | "LifecycleOption"
        "projectLifecycle"           | property                | [LifecycleOption.production, LifecycleOption.development] | _                                | "LifecycleOption..."

        "projectBusinessCriticality" | toProviderSet(property) | [BusinessCriticalityOption.critical]                      | _                                | "List<BusinessCriticalityOption>"
        "projectBusinessCriticality" | toProviderSet(property) | [BusinessCriticalityOption.high]                          | _                                | "Provider<List<BusinessCriticalityOption>>"
        "projectBusinessCriticality" | toSetter(property)      | [BusinessCriticalityOption.low]                           | _                                | "List<BusinessCriticalityOption>"
        "projectBusinessCriticality" | toSetter(property)      | [BusinessCriticalityOption.medium]                        | _                                | "Provider<List<BusinessCriticalityOption>>"
        "projectBusinessCriticality" | property                | [BusinessCriticalityOption.critical]                      | _                                | "List<BusinessCriticalityOption>"
        "projectBusinessCriticality" | property                | BusinessCriticalityOption.high                            | [BusinessCriticalityOption.high] | "BusinessCriticalityOption"
        "projectBusinessCriticality" | property                | [BusinessCriticalityOption.low]                           | _                                | "BusinessCriticalityOption..."

        "projectTags"                | toProviderSet(property) | ["foo": "test", "bar": "baz"]                             | _                                | "Map<String, String>"
        "projectTags"                | toProviderSet(property) | ["foo": "test", "bar": "baz"]                             | _                                | "Provider<Map<String, String>>"
        "projectTags"                | toSetter(property)      | ["foo": "test", "bar": "baz"]                             | _                                | "Map<String, String>"
        "projectTags"                | toSetter(property)      | ["foo": "test", "bar": "baz"]                             | _                                | "Provider<Map<String, String>>"
        "projectTags"                | property                | ["foo": "test", "bar": "baz"]                             | _                                | "Map<String, String>"
        "projectTags"                | property                | ["foo": "test", "bar": "baz"]                             | _                                | "Provider<Map<String, String>>"
        "projectTags"                | property                | "foo= test, bar=baz"                                      | ["foo": "test", "bar": "baz"]    | "String"
        "projectTags"                | "tags"                  | " foo=test,bar= baz "                                     | ["foo": "test", "bar": "baz"]    | "String"

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
        commandlineFlag                  | _
        "--project-business-criticality" | _
        "--project-environment"          | _
        "--project-lifecycle"            | _
        "--project-tags"                 | _
        "--tags"                         | _
        "--trust-policies"               | _
    }
}
