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
import wooga.gradle.snyk.SnykIntegrationSpec
import wooga.gradle.snyk.SnykPlugin

import java.lang.reflect.ParameterizedType

import static com.wooga.gradle.test.PropertyUtils.toProviderSet
import static com.wooga.gradle.test.PropertyUtils.toSetter

abstract class SnykTaskIntegrationSpec<T extends SnykTask> extends SnykIntegrationSpec {
    Class<T> getSubjectUnderTestClass() {
        if (!_sutClass) {
            try {
                this._sutClass = (Class<T>) ((ParameterizedType) this.getClass().getGenericSuperclass())
                        .getActualTypeArguments()[0];
            }
            catch (Exception e) {
                this._sutClass = (Class<T>) SnykTask
            }
        }
        _sutClass
    }
    private Class<T> _sutClass

    @Override
    String getSubjectUnderTestName() {
        "${subjectUnderTestClass.simpleName.uncapitalize()}Test"
    }

    @Override
    String getSubjectUnderTestTypeName() {
        subjectUnderTestClass.getTypeName()
    }

    def setup() {
        buildFile << """
        ${applyPlugin(SnykPlugin)}
        
        task $subjectUnderTestName(type: ${subjectUnderTestTypeName})
        """.stripIndent()
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
        property     | method                  | rawValue             | returnValue | type
        "token"      | toProviderSet(property) | "some_token"         | _           | "String"
        "token"      | toProviderSet(property) | "some_token"         | _           | "Provider<String>"
        "token"      | toSetter(property)      | "some_token"         | _           | "String"
        "token"      | toSetter(property)      | "some_token"         | _           | "Provider<String>"

        "logFile"    | toProviderSet(property) | "/path/to/logFile"   | _           | "File"
        "logFile"    | toProviderSet(property) | "/path/to/logFile"   | _           | "Provider<RegularFile>"
        "logFile"    | toSetter(property)      | "/path/to/logFile"   | _           | "File"
        "logFile"    | toSetter(property)      | "/path/to/logFile"   | _           | "Provider<RegularFile>"

        "executable" | toProviderSet(property) | "snyk1"              | _           | "String"
        "executable" | toProviderSet(property) | "snyk2"              | _           | "Provider<String>"
        "executable" | toSetter(property)      | "snyk3"              | _           | "String"
        "executable" | toSetter(property)      | "snyk4"              | _           | "Provider<String>"

        "snykPath"   | toProviderSet(property) | "/path/to/snyk_home" | _           | "File"
        "snykPath"   | toProviderSet(property) | "/path/to/snyk_home" | _           | "Provider<Directory>"
        "snykPath"   | toSetter(property)      | "/path/to/snyk_home" | _           | "File"
        "snykPath"   | toSetter(property)      | "/path/to/snyk_home" | _           | "Provider<Directory>"

        "insecure"   | toProviderSet(property) | true                 | _           | "Boolean"
        "insecure"   | toProviderSet(property) | false                | _           | "Provider<Boolean>"
        "insecure"   | toSetter(property)      | true                 | _           | "Boolean"
        "insecure"   | toSetter(property)      | false                | _           | "Provider<Boolean>"

        "debug"      | toProviderSet(property) | true                 | _           | "Boolean"
        "debug"      | toProviderSet(property) | false                | _           | "Provider<Boolean>"
        "debug"      | toSetter(property)      | true                 | _           | "Boolean"
        "debug"      | toSetter(property)      | false                | _           | "Provider<Boolean>"
        value = wrapValueBasedOnType(rawValue, type, wrapValueFallback)
        expectedValue = returnValue == _ ? rawValue : returnValue
    }
}
