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

import com.wooga.gradle.test.PropertyLocation
import com.wooga.gradle.test.PropertyQueryTaskWriter
import com.wooga.gradle.test.queries.TestValue
import com.wooga.gradle.test.writers.PropertyGetterTaskWriter
import com.wooga.gradle.test.writers.PropertySetInvocation
import com.wooga.gradle.test.writers.PropertySetterWriter
import spock.lang.Unroll
import wooga.gradle.snyk.cli.BusinessCriticalityOption
import wooga.gradle.snyk.cli.EnvironmentOption
import wooga.gradle.snyk.cli.LifecycleOption

class MonitorIntegrationSpec extends SnykCheckBaseIntegrationSpec<Monitor> {

    @Override
    String getCommandName() {
        "monitor"
    }

    @Unroll("can set property #property with cli option #cliOption")
    def "can set property via cli option"() {

        given: "disable subject under test to no fail"
        appendToSubjectTask("enabled=false")

        expect:
        runPropertyQuery(subjectUnderTestName, getter, setter).matches(rawValue)

        where:
        property                     | cliOption                        | rawValue                                                               | type
        "trustPolicies"              | "--trust-policies"               | true                                                                   | "Boolean"
        "projectEnvironment"         | "--project-environment"          | [EnvironmentOption.backend, EnvironmentOption.frontend]                | "CLIList"
        "projectLifecycle"           | "--project-lifecycle"            | [LifecycleOption.production, LifecycleOption.development]              | "CLIList"
        "projectBusinessCriticality" | "--project-business-criticality" | [BusinessCriticalityOption.critical, BusinessCriticalityOption.medium] | "CLIList"
        "projectTags"                | "--project-tags"                 | ["foo": "test", "bar": "baz"]                                          | "CLIMap"
        "projectTags"                | "--tags"                         | ["foo": "test", "bar": "baz"]                                          | "CLIMap"

        setter = new PropertySetterWriter(subjectUnderTestName, property)
            .set(rawValue, type)
            .toCommandLine(cliOption)
            .serialize(wrapValueFallback)

        getter = new PropertyGetterTaskWriter(setter)
    }

    @Unroll("can set property #property with #method and type #type")
    def "can set property SnykTask"() {

        expect:
        runPropertyQuery(getter, setter).matches(rawValue)

        where:
        property                     | method                                     | rawValue                                                                   | type
        "trustPolicies"              | PropertySetInvocation.providerSet          | true                                                                       | "Boolean"
        "trustPolicies"              | PropertySetInvocation.providerSet          | false                                                                      | "Provider<Boolean>"
        "trustPolicies"              | PropertySetInvocation.setter               | true                                                                       | "Boolean"
        "trustPolicies"              | PropertySetInvocation.setter               | false                                                                      | "Provider<Boolean>"

        "projectEnvironment"         | PropertySetInvocation.providerSet          | [EnvironmentOption.backend]                                                | "List<EnvironmentOption>"
        "projectEnvironment"         | PropertySetInvocation.providerSet          | [EnvironmentOption.backend, EnvironmentOption.frontend]                    | "Provider<List<EnvironmentOption>>"
        "projectEnvironment"         | PropertySetInvocation.setter               | [EnvironmentOption.distributed, EnvironmentOption.onprem]                  | "List<EnvironmentOption>"
        "projectEnvironment"         | PropertySetInvocation.setter               | [EnvironmentOption.internal, EnvironmentOption.hosted]                     | "Provider<List<EnvironmentOption>>"
        "projectEnvironment"         | PropertySetInvocation.method               | [EnvironmentOption.mobile, EnvironmentOption.external]                     | "List<EnvironmentOption>"
        "projectEnvironment"         | PropertySetInvocation.method               | TestValue.set(EnvironmentOption.internal).expectList()                     | "EnvironmentOption"
        "projectEnvironment"         | PropertySetInvocation.method               | [EnvironmentOption.backend, EnvironmentOption.frontend]                    | "EnvironmentOption..."

        "projectLifecycle"           | PropertySetInvocation.providerSet          | [LifecycleOption.production, LifecycleOption.development]                  | "List<LifecycleOption>"
        "projectLifecycle"           | PropertySetInvocation.providerSet          | [LifecycleOption.sandbox, LifecycleOption.development]                     | "Provider<List<LifecycleOption>>"
        "projectLifecycle"           | PropertySetInvocation.setter               | [LifecycleOption.production, LifecycleOption.sandbox]                      | "List<LifecycleOption>"
        "projectLifecycle"           | PropertySetInvocation.setter               | [LifecycleOption.production, LifecycleOption.development]                  | "Provider<List<LifecycleOption>>"
        "projectLifecycle"           | PropertySetInvocation.method               | [LifecycleOption.production, LifecycleOption.development]                  | "List<LifecycleOption>"
        "projectLifecycle"           | PropertySetInvocation.method               | TestValue.set(LifecycleOption.sandbox).expectList()                        | "LifecycleOption"
        "projectLifecycle"           | PropertySetInvocation.method               | [LifecycleOption.production, LifecycleOption.development]                  | "LifecycleOption..."

        "projectBusinessCriticality" | PropertySetInvocation.providerSet          | [BusinessCriticalityOption.critical]                                       | "List<BusinessCriticalityOption>"
        "projectBusinessCriticality" | PropertySetInvocation.providerSet          | [BusinessCriticalityOption.high]                                           | "Provider<List<BusinessCriticalityOption>>"
        "projectBusinessCriticality" | PropertySetInvocation.setter               | [BusinessCriticalityOption.low]                                            | "List<BusinessCriticalityOption>"
        "projectBusinessCriticality" | PropertySetInvocation.setter               | [BusinessCriticalityOption.medium]                                         | "Provider<List<BusinessCriticalityOption>>"
        "projectBusinessCriticality" | PropertySetInvocation.method               | [BusinessCriticalityOption.critical]                                       | "List<BusinessCriticalityOption>"
        "projectBusinessCriticality" | PropertySetInvocation.method               | TestValue.set(BusinessCriticalityOption.high).expectList()                 | "BusinessCriticalityOption"
        "projectBusinessCriticality" | PropertySetInvocation.method               | [BusinessCriticalityOption.low]                                            | "BusinessCriticalityOption..."

        "projectTags"                | PropertySetInvocation.providerSet          | ["foo": "test", "bar": "baz"]                                              | "Map<String, String>"
        "projectTags"                | PropertySetInvocation.providerSet          | ["foo": "test", "bar": "baz"]                                              | "Provider<Map<String, String>>"
        "projectTags"                | PropertySetInvocation.setter               | ["foo": "test", "bar": "baz"]                                              | "Map<String, String>"
        "projectTags"                | PropertySetInvocation.setter               | ["foo": "test", "bar": "baz"]                                              | "Provider<Map<String, String>>"
        "projectTags"                | PropertySetInvocation.method               | ["foo": "test", "bar": "baz"]                                              | "Map<String, String>"
        "projectTags"                | PropertySetInvocation.method               | ["foo": "test", "bar": "baz"]                                              | "Provider<Map<String, String>>"
        "projectTags"                | PropertySetInvocation.method               | TestValue.set("foo= test, bar=baz").expect(["foo": "test", "bar": "baz"])  | "String"
        "projectTags"                | PropertySetInvocation.customSetter("tags") | TestValue.set(" foo=test,bar= baz ").expect(["foo": "test", "bar": "baz"]) | "String"

        setter = new PropertySetterWriter(subjectUnderTestName, property)
            .set(rawValue, type)
            .toScript(method)
            .serialize(wrapValueFallback)

        getter = new PropertyGetterTaskWriter(setter)
    }

    @Unroll()
    def "composes correct CLI string from setters #prop -> #expected"() {

        given: "a snyk wrapper"
        setSnykWrapper(true, subjectUnderTestName)

        and: "a set of properties being set onto the task"
        buildFile << "\n${subjectUnderTestName}.${prop}"

        when:
        def result = runTasksSuccessfully(subjectUnderTestName)

        then:
        outputContains(result, expected)

        where:
        prop                                             | flags
        "trustPolicies=false"                            | ""
        "trustPolicies=true"                             | "--trust-policies"
        "projectEnvironment=\"frontend\""                | "--project-environment=frontend"
        "projectEnvironment=['frontend','backend']"      | "--project-environment=frontend,backend"
        "projectLifecycle='sandbox'"                     | "--project-lifecycle=sandbox"
        "projectLifecycle=['production','sandbox']"      | "--project-lifecycle=production,sandbox"
        "projectBusinessCriticality='critical'"          | "--project-business-criticality=critical"
        "projectBusinessCriticality=['high','low']"      | "--project-business-criticality=high,low"
        "projectTags=['dept':'finance', 'team':'alpha']" | "--project-tags=dept=finance,team=alpha"

        expected = flags.empty ? "monitor" : "monitor ${flags}"
    }
}
