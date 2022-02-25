package wooga.gradle.snyk

import com.wooga.gradle.test.ConventionSource
import com.wooga.gradle.test.PropertyQueryTaskWriter
import com.wooga.gradle.test.PropertyUtils
import spock.lang.Unroll
import wooga.gradle.snyk.cli.*

abstract class SnykPluginRegistrationIntegrationSpec extends SnykIntegrationSpec {
    def setup() {
        buildFile << """
        ${applyPlugin(SnykPlugin)}
        """
    }

    abstract String getPackageId()

    abstract String getRegisterProjectInvocation(String projectId)

    abstract String getGeneratedExtensionName()

    abstract String getGeneratedTaskName(String baseTaskName)

    abstract File getRegisteredBuildFile()

    abstract void setupProject()

    abstract String getCustomPackageFileValue()

    abstract String getCustomProjectNameValue()

    abstract String getCustomSubProjectValue()

    @Unroll
    def "can configure property #property for registered project"() {
        given: "a registered gradle sub project"
        setupProject()
        buildFile << """
        ${extensionName} {
            ${getRegisterProjectInvocation(packageId)} {
                ${property} = ${value}
            }
        }
        """.stripIndent()

        when:
        def query = new PropertyQueryTaskWriter("project.extensions.getByName('${generatedExtensionName}').${property}",
                ".getOrNull()",
                PropertyUtils.toCamelCase("query_${generatedExtensionName}.${property}")
        )

        query.write(registeredBuildFile)
        def result = runTasksSuccessfully(query.taskName)

        then:
        query.matches(result, testValue)

        where:
        property                         | rawValue                             | type                              || expectedValue
        "packageFile"                    | osPath("/some/package/file")         | "RegularFile"                     || _
        "projectName"                    | "testProjectName"                    | "String"                          || _
        "subProject"                     | "Sub1"                               | "String"                          || _
        "allProjects"                    | true                                 | "Boolean"                         || _
        "detectionDepth"                 | 22                                   | "Integer"                         || _
        "exclude"                        | [osPath('/path/exclude1')]           | "List<File>"                      || _
        "pruneRepeatedSubDependencies"   | true                                 | "Boolean"                         || _
        "printDependencies"              | true                                 | "Boolean"                         || _
        "remoteRepoUrl"                  | "http://remote/url1"                 | "String"                          || _
        "includeDevelopmentDependencies" | true                                 | "Boolean"                         || _
        "orgName"                        | "org1"                               | "String"                          || _
        "ignorePolicy"                   | true                                 | "Boolean"                         || _
        "showVulnerablePaths"            | VulnerablePathsOption.some           | "VulnerablePathsOption"           || _
        "targetReference"                | "reference1"                         | "String"                          || _
        "policyPath"                     | osPath("/path/to/policy1")           | "RegularFile"                     || _
        "printJson"                      | true                                 | "Boolean"                         || _
        "jsonOutputPath"                 | osPath("/path/to/json1")             | "RegularFile"                     || _
        "printSarif"                     | true                                 | "Boolean"                         || _
        "sarifOutputPath"                | osPath("/path/to/sarif1")            | "RegularFile"                     || _
        "severityThreshold"              | SeverityThresholdOption.critical     | "SeverityThresholdOption"         || _
        "failOn"                         | FailOnOption.all                     | "FailOnOption"                    || _
        "compilerArguments"              | ['--flag1']                          | "List<String>"                    || _

        "assetsProjectName"              | true                                 | "Boolean"                         || _
        "packagesFolder"                 | osPath("/path/to/packages1")         | "Directory"                       || _
        "projectNamePrefix"              | "prefix1"                            | "String"                          || _

        "allSubProjects"                 | true                                 | "Boolean"                         || _
        "configurationMatching"          | "config1"                            | "String"                          || _
        "configurationAttributes"        | ['attribute1']                       | "List<String>"                    || _
        "reachable"                      | true                                 | "Boolean"                         || _
        "reachableTimeout"               | 22                                   | "Integer"                         || _
        "initScript"                     | osPath("/path/to/initScript1")       | "RegularFile"                     || _

        "scanAllUnmanaged"               | true                                 | "Boolean"                         || _

        "strictOutOfSync"                | true                                 | "Boolean"                         || _

        "command"                        | "command1"                           | "String"                          || _
        "skipUnresolved"                 | true                                 | "Boolean"                         || _

        "insecure"                       | true                                 | "Boolean"                         || _
        "debug"                          | true                                 | "Boolean"                         || _

        "token"                          | "test_token_1"                       | "String"                          || _

        "executableName"                 | "snyk1"                              | "String"                          || _
        "snykPath"                       | osPath("/path/to/snyk_home_1")       | "Directory"                       || _

        "trustPolicies"                  | true                                 | "Boolean"                         || _
        "projectLifecycle"               | [LifecycleOption.development]        | "List<LifecycleOption>"           || _
        "projectBusinessCriticality"     | [BusinessCriticalityOption.critical] | "List<BusinessCriticalityOption>" || _
        "projectTags"                    | ["foo": "bar"]                       | "Map<String, String>"             || _

        value = (type != _) ? wrapValueBasedOnType(rawValue, type.toString(), wrapValueFallback) : rawValue
        testValue = (expectedValue == _) ? rawValue : expectedValue
    }

    @Unroll
    def "registered project extension sets custom value for property '#property'"() {
        given: "a registered gradle sub project"
        setupProject()
        buildFile << """
        ${extensionName} {
            ${getRegisterProjectInvocation(packageId)}
            ${property} = ${rootExtensionValue}
        }
        """.stripIndent()

        when:
        def query = new PropertyQueryTaskWriter("project.extensions.getByName('${generatedExtensionName}').${property}",
                ".getOrNull()",
                PropertyUtils.toCamelCase("query_${generatedExtensionName}.${property}")
        )
        query.write(registeredBuildFile)
        def result = runTasksSuccessfully(query.taskName)

        then:
        def v = (testValue && String.isInstance(testValue)) ? testValue
                .replaceAll("#projectName#", moduleName)
                .replaceAll("#packageName#", packageId)
                .replaceAll("#subProjectName#", packageId)
                .replace("#projectDir#", projectDir.absolutePath) : testValue
        query.matches(result, v)

        when:
        query = new PropertyQueryTaskWriter("${extensionName}.${property}")
        query.write(buildFile)
        result = runTasksSuccessfully(":${query.taskName}")

        then:
        def v2 = (testValue && String.isInstance(testValue)) ? testValue
                .replaceAll("#projectName#", moduleName)
                .replaceAll("#packageName#", packageId)
                .replaceAll("#subProjectName#", packageId)
                .replace("#projectDir#", projectDir.absolutePath) : testValue
        !query.matches(result, v2)

        where:
        property      | rawValue               | type          | rawRootExtensionValue   || expectedValue
        "packageFile" | customPackageFileValue | "RegularFile" | "/path/to/some/package" || _
        "projectName" | customProjectNameValue | "String"      | "someName"              || _
        "subProject"  | customSubProjectValue  | "String"      | "someSubProjectName"    || _

        value = (type != _) ? wrapValueBasedOnType(rawValue, type.toString(), wrapValueFallback) : rawValue
        rootExtensionValue = (type != _) ? wrapValueBasedOnType(rawRootExtensionValue, type.toString(), wrapValueFallback) : rawRootExtensionValue
        testValue = (expectedValue == _) ? rawValue : expectedValue
    }

    @Unroll
    def "registered project inherits conventions from root extension for property '#property'"() {
        given: "a registered gradle sub project"
        setupProject()
        buildFile << """
        ${extensionName} {
            ${getRegisterProjectInvocation(packageId)}
        }
        """.stripIndent()

        and: "a value set in the root extension"
        conventionSource.write(buildFile, value.toString())

        when:
        def query = new PropertyQueryTaskWriter("project.extensions.getByName('${generatedExtensionName}').${property}",
                ".getOrNull()",
                PropertyUtils.toCamelCase("query_${generatedExtensionName}.${property}")
        )
        query.write(registeredBuildFile)
        def result = runTasksSuccessfully(query.taskName)

        then:
        query.matches(result, testValue)

        where:
        property                         | rawValue                             | type                              | conventionSource                                    | invocation || expectedValue
        "allProjects"                    | true                                 | "Boolean"                         | ConventionSource.extension(extensionName, property) | _          || _
        "detectionDepth"                 | 22                                   | "Integer"                         | ConventionSource.extension(extensionName, property) | _          || _
        "exclude"                        | [osPath('/path/exclude1')]           | "List<File>"                      | ConventionSource.extension(extensionName, property) | _          || _
        "pruneRepeatedSubDependencies"   | true                                 | "Boolean"                         | ConventionSource.extension(extensionName, property) | _          || _
        "printDependencies"              | true                                 | "Boolean"                         | ConventionSource.extension(extensionName, property) | _          || _
        "remoteRepoUrl"                  | "http://remote/url1"                 | "String"                          | ConventionSource.extension(extensionName, property) | _          || _
        "includeDevelopmentDependencies" | true                                 | "Boolean"                         | ConventionSource.extension(extensionName, property) | _          || _
        "orgName"                        | "org1"                               | "String"                          | ConventionSource.extension(extensionName, property) | _          || _
        "ignorePolicy"                   | true                                 | "Boolean"                         | ConventionSource.extension(extensionName, property) | _          || _
        "showVulnerablePaths"            | VulnerablePathsOption.some           | "VulnerablePathsOption"           | ConventionSource.extension(extensionName, property) | _          || _
        "targetReference"                | "reference1"                         | "String"                          | ConventionSource.extension(extensionName, property) | _          || _
        "policyPath"                     | osPath("/path/to/policy1")           | "RegularFile"                     | ConventionSource.extension(extensionName, property) | _          || _
        "printJson"                      | true                                 | "Boolean"                         | ConventionSource.extension(extensionName, property) | _          || _
        "jsonOutputPath"                 | osPath("/path/to/json1")             | "RegularFile"                     | ConventionSource.extension(extensionName, property) | _          || _
        "printSarif"                     | true                                 | "Boolean"                         | ConventionSource.extension(extensionName, property) | _          || _
        "sarifOutputPath"                | osPath("/path/to/sarif1")            | "RegularFile"                     | ConventionSource.extension(extensionName, property) | _          || _
        "severityThreshold"              | SeverityThresholdOption.critical     | "SeverityThresholdOption"         | ConventionSource.extension(extensionName, property) | _          || _
        "failOn"                         | FailOnOption.all                     | "FailOnOption"                    | ConventionSource.extension(extensionName, property) | _          || _
        "compilerArguments"              | ['--flag1']                          | "List<String>"                    | ConventionSource.extension(extensionName, property) | _          || _

        "assetsProjectName"              | true                                 | "Boolean"                         | ConventionSource.extension(extensionName, property) | _          || _
        "packagesFolder"                 | osPath("/path/to/packages1")         | "Directory"                       | ConventionSource.extension(extensionName, property) | _          || _
        "projectNamePrefix"              | "prefix1"                            | "String"                          | ConventionSource.extension(extensionName, property) | _          || _

        "allSubProjects"                 | true                                 | "Boolean"                         | ConventionSource.extension(extensionName, property) | _          || _
        "configurationMatching"          | "config1"                            | "String"                          | ConventionSource.extension(extensionName, property) | _          || _
        "configurationAttributes"        | ['attribute1']                       | "List<String>"                    | ConventionSource.extension(extensionName, property) | _          || _
        "reachable"                      | true                                 | "Boolean"                         | ConventionSource.extension(extensionName, property) | _          || _
        "reachableTimeout"               | 22                                   | "Integer"                         | ConventionSource.extension(extensionName, property) | _          || _
        "initScript"                     | osPath("/path/to/initScript1")       | "RegularFile"                     | ConventionSource.extension(extensionName, property) | _          || _

        "scanAllUnmanaged"               | true                                 | "Boolean"                         | ConventionSource.extension(extensionName, property) | _          || _

        "strictOutOfSync"                | true                                 | "Boolean"                         | ConventionSource.extension(extensionName, property) | _          || _

        "command"                        | "command1"                           | "String"                          | ConventionSource.extension(extensionName, property) | _          || _
        "skipUnresolved"                 | true                                 | "Boolean"                         | ConventionSource.extension(extensionName, property) | _          || _

        "insecure"                       | true                                 | "Boolean"                         | ConventionSource.extension(extensionName, property) | _          || _
        "debug"                          | true                                 | "Boolean"                         | ConventionSource.extension(extensionName, property) | _          || _

        "token"                          | "test_token_1"                       | "String"                          | ConventionSource.extension(extensionName, property) | _          || _

        "executableName"                 | "snyk1"                              | "String"                          | ConventionSource.extension(extensionName, property) | _          || _
        "snykPath"                       | osPath("/path/to/snyk_home_1")       | "Directory"                       | ConventionSource.extension(extensionName, property) | _          || _

        "trustPolicies"                  | true                                 | "Boolean"                         | ConventionSource.extension(extensionName, property) | _          || _
        "projectLifecycle"               | [LifecycleOption.development]        | "List<LifecycleOption>"           | ConventionSource.extension(extensionName, property) | _          || _
        "projectBusinessCriticality"     | [BusinessCriticalityOption.critical] | "List<BusinessCriticalityOption>" | ConventionSource.extension(extensionName, property) | _          || _
        "projectTags"                    | ["foo": "bar"]                       | "Map<String, String>"             | ConventionSource.extension(extensionName, property) | _          || _

        value = (type != _) ? wrapValueBasedOnType(rawValue, type.toString(), wrapValueFallback) : rawValue
        testValue = (expectedValue == _) ? rawValue : expectedValue
    }

    @Unroll
    def "can access task #taskName from generated project extension"() {
        setupProject()
        buildFile << """
        ${extensionName} {
            ${getRegisterProjectInvocation(packageId)} {
                ${taskName} {
                    ${property} = ${value}
                }
            }
        }
        """.stripIndent()

        when:
        def query = new PropertyQueryTaskWriter("project.extensions.getByName('${generatedExtensionName}').${taskName}.flatMap{it.${property}}",
                ".getOrNull()",
                PropertyUtils.toCamelCase("query_${generatedExtensionName}.${taskName}.${property}")
        )

        query.write(registeredBuildFile)
        def result = runTasksSuccessfully(query.taskName)

        then:
        query.matches(result, testValue)

        when: "when checking the value at the generated task directly"
        query = new PropertyQueryTaskWriter("project.tasks.getByName('${getGeneratedTaskName(taskName)}').${property}",
                ".getOrNull()",
                PropertyUtils.toCamelCase("query_${getGeneratedTaskName(taskName)}.${property}")
        )

        query.write(registeredBuildFile)
        result = runTasksSuccessfully(query.taskName)

        then:
        query.matches(result, testValue)

        where:
        taskName      | property | rawValue | type     || expectedValue
        "snykTest"    | "token"  | "123456" | "String" || _
        "snykMonitor" | "token"  | "123456" | "String" || _
        "snykReport"  | "token"  | "123456" | "String" || _

        value = (type != _) ? wrapValueBasedOnType(rawValue, type.toString(), wrapValueFallback) : rawValue
        testValue = (expectedValue == _) ? rawValue : expectedValue
    }
}
