package wooga.gradle.snyk

import com.wooga.gradle.test.PropertyLocation
import com.wooga.gradle.test.PropertyQueryTaskWriter
import spock.lang.Unroll
import wooga.gradle.snyk.tasks.Monitor
import wooga.gradle.snyk.tasks.Report
import wooga.gradle.snyk.tasks.Test

import static com.wooga.gradle.test.PropertyUtils.*
import static com.wooga.gradle.test.SpecUtils.escapedPath

class SnykStrategyIntegrationSpec extends SnykIntegrationSpec {

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
        property          | method                  | rawValue                     | expectedValue      | type                     | location
        "strategies"      | _                       | _                            | null               | "Provider<List<String>>" | PropertyLocation.none
        "strategies"      | _                       | ["test1", "test2"].join(',') | ["test1", "test2"] | _                        | PropertyLocation.environment
        "strategies"      | _                       | ["test1", "test2"].join(',') | ["test1", "test2"] | _                        | PropertyLocation.property
        "strategies"      | toSetter(property)      | ["test1", "test2"]           | _                  | "Provider<List<String>>" | PropertyLocation.script
        "strategies"      | toSetter(property)      | ["test1", "test2"]           | _                  | "List<String>"           | PropertyLocation.script
        "strategies"      | toProviderSet(property) | ["test1", "test2"]           | _                  | "Provider<List<String>>" | PropertyLocation.script
        "strategies"      | toProviderSet(property) | ["test1", "test2"]           | _                  | "List<String>"           | PropertyLocation.script
        "strategies"      | "strategy"              | "test1"                      | ["test1"]          | "String"                 | PropertyLocation.script
        "strategies"      | "strategy"              | "test1"                      | ["test1"]          | "Provider<String>"       | PropertyLocation.script
        "strategies"      | property                | ["test1", "test2"]           | ["test1", "test2"] | "String..."              | PropertyLocation.script
        "strategies"      | property                | ["test1", "test2"]           | ["test1", "test2"] | "List<String>"           | PropertyLocation.script

        "checkTaskName"   | _                       | _                            | "check"            | "Provider<String>"       | PropertyLocation.none
        "checkTaskName"   | _                       | "check1"                     | _                  | _                        | PropertyLocation.environment
        "checkTaskName"   | _                       | "check2"                     | _                  | _                        | PropertyLocation.property
        "checkTaskName"   | toSetter(property)      | "check3"                     | _                  | "Provider<String>"       | PropertyLocation.script
        "checkTaskName"   | toSetter(property)      | "check4"                     | _                  | "String"                 | PropertyLocation.script
        "checkTaskName"   | toProviderSet(property) | "check5"                     | _                  | "Provider<String>"       | PropertyLocation.script
        "checkTaskName"   | toProviderSet(property) | "check6"                     | _                  | "String"                 | PropertyLocation.script

        "publishTaskName" | _                       | _                            | "publish"          | "Provider<String>"       | PropertyLocation.none
        "publishTaskName" | _                       | "check1"                     | _                  | _                        | PropertyLocation.environment
        "publishTaskName" | _                       | "check2"                     | _                  | _                        | PropertyLocation.property
        "publishTaskName" | toSetter(property)      | "check3"                     | _                  | "Provider<String>"       | PropertyLocation.script
        "publishTaskName" | toSetter(property)      | "check4"                     | _                  | "String"                 | PropertyLocation.script
        "publishTaskName" | toProviderSet(property) | "check5"                     | _                  | "Provider<String>"       | PropertyLocation.script
        "publishTaskName" | toProviderSet(property) | "check6"                     | _                  | "String"                 | PropertyLocation.script

        value = (type != _) ? wrapValueBasedOnType(rawValue, type.toString(), wrapValueFallback) : rawValue
        providedValue = (location == PropertyLocation.script) ? type : value
        testValue = (expectedValue == _) ? rawValue : expectedValue
        reason = location.reason() + ((location == PropertyLocation.none) ? "" : "  with '$providedValue' ")
        escapedValue = (value instanceof String) ? escapedPath(value) : value
        invocation = (method != _) ? "${method}(${escapedValue})" : "${property} = ${escapedValue}"
    }

    @Unroll
    def "#snykTaskName hooks into #hookTaskName when strategy #strategy is set"() {
        given: "an empty task for the hook taskName"
        buildFile << """
        task(${hookTaskName})
        """.stripIndent()

        and: "fake snyk executable"
        setSnykWrapper()
        buildFile << """
        ${extensionName}.token = "1234567"
        """

        and:
        buildFile << """
        snyk.strategies.set(${wrapValueBasedOnType(strategy, "List<String>")})
        """.stripIndent()

        and: "custom additional snyk tasks"
        snykTaskNames.each {
            buildFile << """
            task("${it}", type: ${snykTaskType.name})
            """.stripIndent()
        }

        when:
        def result = runTasks(hookTaskName)

        then:
        result.wasExecuted(hookTaskName)
        result.wasExecuted(snykTaskName)
        snykTaskNames.each {
            assert result.wasExecuted(it)
        }

        where:
        snykTaskName  | snykTaskNames                         | snykTaskType | hookTaskName | strategy
        "snykMonitor" | ["snykMonitor2", "snykMonitorCustom"] | Monitor      | "check"      | ["monitor_check"]
        "snykTest"    | ["snykTest2", "snykTestCustom"]       | Test         | "check"      | ["test_check"]
        "snykReport"  | ["snykReport2", "snykReportCustom"]   | Report       | "check"      | ["report_check"]
        "snykMonitor" | ["snykMonitor2", "snykMonitorCustom"] | Monitor      | "publish"    | ["monitor_publish"]
        "snykTest"    | ["snykTest2", "snykTestCustom"]       | Test         | "publish"    | ["test_publish"]
        "snykReport"  | ["snykReport2", "snykReportCustom"]   | Report       | "publish"    | ["report_publish"]
    }
}
