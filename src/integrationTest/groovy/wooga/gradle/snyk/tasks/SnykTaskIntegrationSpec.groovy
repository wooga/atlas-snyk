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
import com.wooga.gradle.test.TaskIntegrationSpec
import com.wooga.gradle.test.writers.PropertyGetterTaskWriter
import com.wooga.gradle.test.writers.PropertySetInvocation
import com.wooga.gradle.test.writers.PropertySetterWriter
import org.gradle.api.file.Directory
import spock.lang.Unroll
import wooga.gradle.snyk.SnykIntegrationSpec

import java.lang.reflect.ParameterizedType

import static com.wooga.gradle.PlatformUtils.escapedPath
import static com.wooga.gradle.test.PropertyUtils.toProviderSet
import static com.wooga.gradle.test.PropertyUtils.toSetter

abstract class SnykTaskIntegrationSpec<T extends SnykTask> extends SnykIntegrationSpec implements TaskIntegrationSpec<T> {

    def setup() {
        environmentVariables.set("SNYK_TOKEN", System.getenv("ATLAS_SNYK_INTEGRATION_TOKEN"))
        buildFile << """
        
        task $subjectUnderTestName(type: ${subjectUnderTestTypeName})
        """.stripIndent()
    }

    void setSnykWrapper(Boolean setDummyToken = true, String object = extensionName, Boolean printEnvironment = false, Boolean logToStdout = false, exitValue = 0) {
        def snykWrapper = generateBatchWrapper("snyk-wrapper", printEnvironment)
        def lines = snykWrapper.readLines().dropRight(1)
        snykWrapper.text = lines.join("\n")
        snykWrapper << "exit ${exitValue}"
        def wrapperDir = snykWrapper.parent
        def wrapperPath = escapedPath(wrapperDir)

        buildFile << """
            ${object}.executableName=${wrapValueBasedOnType(snykWrapper.name, String)}
            ${object}.snykPath=${wrapValueBasedOnType(wrapperPath, Directory)}
            ${object}.logToStdout=${wrapValueBasedOnType(logToStdout, Boolean)}
        """.stripIndent()

        if (setDummyToken) {
            buildFile << """
                ${object}.token=${wrapValueBasedOnType("foobar", String)}
            """.stripIndent()
        }
    }

    @Unroll("can set property #property with cli option #cliOption")
    def "can set property via cli option"() {

        given: "disable subject under test to no fail"
        appendToSubjectTask("enabled=false")

        expect:
        runPropertyQuery(subjectUnderTestName, getter, setter).matches(rawValue)

        where:
        property   | cliOption    | rawValue | type
        "insecure" | "--insecure" | true     | "Boolean"

        setter = new PropertySetterWriter(subjectUnderTestName, property)
            .set(rawValue, type)
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
        property          | method                            | rawValue                     | type
        "token"           | PropertySetInvocation.providerSet | "some_token"                 | "String"
        "token"           | PropertySetInvocation.providerSet | "some_token"                 | "Provider<String>"
        "token"           | PropertySetInvocation.setter      | "some_token"                 | "String"
        "token"           | PropertySetInvocation.setter      | "some_token"                 | "Provider<String>"

        "logFile"         | PropertySetInvocation.providerSet | osPath("/path/to/logFile")   | "File"
        "logFile"         | PropertySetInvocation.providerSet | osPath("/path/to/logFile")   | "Provider<RegularFile>"
        "logFile"         | PropertySetInvocation.setter      | osPath("/path/to/logFile")   | "File"
        "logFile"         | PropertySetInvocation.setter      | osPath("/path/to/logFile")   | "Provider<RegularFile>"

        "executableName"  | PropertySetInvocation.providerSet | "snyk1"                      | "String"
        "executableName"  | PropertySetInvocation.providerSet | "snyk2"                      | "Provider<String>"
        "executableName"  | PropertySetInvocation.setter      | "snyk3"                      | "String"
        "executableName"  | PropertySetInvocation.setter      | "snyk4"                      | "Provider<String>"

        "snykPath"        | PropertySetInvocation.providerSet | osPath("/path/to/snyk_home") | "File"
        "snykPath"        | PropertySetInvocation.providerSet | osPath("/path/to/snyk_home") | "Provider<Directory>"
        "snykPath"        | PropertySetInvocation.setter      | osPath("/path/to/snyk_home") | "File"
        "snykPath"        | PropertySetInvocation.setter      | osPath("/path/to/snyk_home") | "Provider<Directory>"

        "insecure"        | PropertySetInvocation.providerSet | true                         | "Boolean"
        "insecure"        | PropertySetInvocation.providerSet | false                        | "Provider<Boolean>"
        "insecure"        | PropertySetInvocation.setter      | true                         | "Boolean"
        "insecure"        | PropertySetInvocation.setter      | false                        | "Provider<Boolean>"

        "debug"           | PropertySetInvocation.providerSet | true                         | "Boolean"
        "debug"           | PropertySetInvocation.providerSet | false                        | "Provider<Boolean>"
        "debug"           | PropertySetInvocation.setter      | true                         | "Boolean"
        "debug"           | PropertySetInvocation.setter      | false                        | "Provider<Boolean>"

        "ignoreExitValue" | PropertySetInvocation.providerSet | true                         | "Boolean"
        "ignoreExitValue" | PropertySetInvocation.providerSet | false                        | "Provider<Boolean>"
        "ignoreExitValue" | PropertySetInvocation.setter      | true                         | "Boolean"
        "ignoreExitValue" | PropertySetInvocation.setter      | false                        | "Provider<Boolean>"

        setter = new PropertySetterWriter(subjectUnderTestName, property)
            .set(rawValue, type)
            .toScript(method)
            .serialize(wrapValueFallback)

        getter = new PropertyGetterTaskWriter(setter)
    }

    @Unroll()
    def "composes correct CLI environment from #setter -> #expected"() {
        given: "a snyk wrapper"
        setSnykWrapper(false, subjectUnderTestName, true, true)

        and: "a set of properties being set onto the task"
        if (setter.concat("file")) {
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
        setter            | environment
        "token='1234567'" | "SNYK_TOKEN=1234567"

        expected = environment
        mockFile = "foo.bar"
    }

    def "task is never up to date"() {
        given: "a set snyk wrapper"
        setSnykWrapper(true, subjectUnderTestName)

        when:
        def firstRun = runTasks(subjectUnderTestName)
        def secondRun = runTasks(subjectUnderTestName)

        then:
        firstRun.success
        !firstRun.wasUpToDate(subjectUnderTestName)
        secondRun.success
        !secondRun.wasUpToDate(subjectUnderTestName)
    }

    @Unroll
    def "task #taskTypeName with exit value #exitValue will #message"() {
        given: "a set snyk wrapper"
        setSnykWrapper(true, subjectUnderTestName, false, false, exitValue)
        appendToSubjectTask("""
        ignoreExitValue = true
        """.stripIndent())

        when:
        def result = runTasks(subjectUnderTestName)

        then:
        result.success
        result.wasExecuted(subjectUnderTestName)

        where:
        ignoreExitValue | expectSuccess | message
        true            | true          | "succeed when ignoreExitValue is set"
        false           | false         | "fail when ignoreExitValue is not set"
        exitValue = 1
        taskTypeName = subjectUnderTestTypeName
    }
}
