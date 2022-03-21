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
    def "sets convention for task #taskName and property #property"() {
        given: "a registered gradle sub project"
        setupProject()
        buildFile << """
        ${extensionName} {
            ${getRegisterProjectInvocation(packageId)} {
                ${conventionSource} = ${value}
            }
        }
        """.stripIndent()

        when:
        def query = new PropertyQueryTaskWriter("project.extensions.getByName('${generatedExtensionName}').${taskName}.get().${property}",
                (invocation == _) ? ".getOrNull()" : invocation.toString(),
                PropertyUtils.toCamelCase("query_${generatedExtensionName}_${taskName}.${property}")
        )

        query.write(registeredBuildFile)
        def result = runTasksSuccessfully(query.taskName)

        then:
        query.matches(result, testValue)

        where:
        taskName      | property                         | rawValue                             | type                              | conventionSource      | invocation || expectedValue
        "snykTest"    | "allProjects"                    | true                                 | "Boolean"                         | property              | _          || _
        "snykReport"  | "allProjects"                    | true                                 | "Boolean"                         | property              | _          || _
        "snykMonitor" | "allProjects"                    | true                                 | "Boolean"                         | property              | _          || _
        "snykTest"    | "detectionDepth"                 | 22                                   | "Integer"                         | property              | _          || _
        "snykReport"  | "detectionDepth"                 | 11                                   | "Integer"                         | property              | _          || _
        "snykMonitor" | "detectionDepth"                 | 45                                   | "Integer"                         | property              | _          || _
        "snykTest"    | "exclude"                        | [osPath('/path/exclude1')]           | "List<File>"                      | property              | _          || _
        "snykReport"  | "exclude"                        | [osPath('/path/exclude2')]           | "List<File>"                      | property              | _          || _
        "snykMonitor" | "exclude"                        | [osPath('/path/exclude3')]           | "List<File>"                      | property              | _          || _
        "snykTest"    | "pruneRepeatedSubDependencies"   | true                                 | "Boolean"                         | property              | _          || _
        "snykReport"  | "pruneRepeatedSubDependencies"   | true                                 | "Boolean"                         | property              | _          || _
        "snykMonitor" | "pruneRepeatedSubDependencies"   | true                                 | "Boolean"                         | property              | _          || _
        "snykTest"    | "printDependencies"              | true                                 | "Boolean"                         | property              | _          || _
        "snykReport"  | "printDependencies"              | true                                 | "Boolean"                         | property              | _          || _
        "snykMonitor" | "printDependencies"              | true                                 | "Boolean"                         | property              | _          || _
        "snykTest"    | "remoteRepoUrl"                  | "http://remote/url1"                 | "String"                          | property              | _          || _
        "snykReport"  | "remoteRepoUrl"                  | "http://remote/url2"                 | "String"                          | property              | _          || _
        "snykMonitor" | "remoteRepoUrl"                  | "http://remote/url3"                 | "String"                          | property              | _          || _
        "snykTest"    | "includeDevelopmentDependencies" | true                                 | "Boolean"                         | property              | _          || _
        "snykReport"  | "includeDevelopmentDependencies" | true                                 | "Boolean"                         | property              | _          || _
        "snykMonitor" | "includeDevelopmentDependencies" | true                                 | "Boolean"                         | property              | _          || _
        "snykTest"    | "orgName"                        | "org1"                               | "String"                          | property              | _          || _
        "snykReport"  | "orgName"                        | "org1"                               | "String"                          | property              | _          || _
        "snykMonitor" | "orgName"                        | "org1"                               | "String"                          | property              | _          || _
        "snykTest"    | "packageFile"                    | osPath("/path/to/package1")          | "RegularFile"                     | property              | _          || _
        "snykReport"  | "packageFile"                    | osPath("/path/to/package2")          | "RegularFile"                     | property              | _          || _
        "snykMonitor" | "packageFile"                    | osPath("/path/to/package3")          | "RegularFile"                     | property              | _          || _
        "snykTest"    | "packageManager"                 | "nuget"                              | "String"                          | property              | _          || _
        "snykReport"  | "packageManager"                 | "gradle"                             | "String"                          | property              | _          || _
        "snykMonitor" | "packageManager"                 | "npm"                                | "String"                          | property              | _          || _
        "snykTest"    | "ignorePolicy"                   | true                                 | "Boolean"                         | property              | _          || _
        "snykReport"  | "ignorePolicy"                   | true                                 | "Boolean"                         | property              | _          || _
        "snykMonitor" | "ignorePolicy"                   | true                                 | "Boolean"                         | property              | _          || _
        "snykTest"    | "showVulnerablePaths"            | VulnerablePathsOption.some           | "VulnerablePathsOption"           | property              | _          || _
        "snykReport"  | "showVulnerablePaths"            | VulnerablePathsOption.none           | "VulnerablePathsOption"           | property              | _          || _
        "snykMonitor" | "showVulnerablePaths"            | VulnerablePathsOption.all            | "VulnerablePathsOption"           | property              | _          || _
        "snykTest"    | "projectName"                    | "project1"                           | "String"                          | property              | _          || _
        "snykReport"  | "projectName"                    | "project2"                           | "String"                          | property              | _          || _
        "snykMonitor" | "projectName"                    | "project3"                           | "String"                          | property              | _          || _
        "snykTest"    | "targetReference"                | "reference1"                         | "String"                          | property              | _          || _
        "snykReport"  | "targetReference"                | "reference2"                         | "String"                          | property              | _          || _
        "snykMonitor" | "targetReference"                | "reference3"                         | "String"                          | property              | _          || _
        "snykTest"    | "policyPath"                     | osPath("/path/to/policy1")           | "RegularFile"                     | property              | _          || _
        "snykReport"  | "policyPath"                     | osPath("/path/to/policy2")           | "RegularFile"                     | property              | _          || _
        "snykMonitor" | "policyPath"                     | osPath("/path/to/policy3")           | "RegularFile"                     | property              | _          || _
        "snykTest"    | "printJson"                      | true                                 | "Boolean"                         | property              | _          || _
        "snykReport"  | "printJson"                      | true                                 | "Boolean"                         | property              | _          || _
        "snykMonitor" | "printJson"                      | true                                 | "Boolean"                         | property              | _          || _
        "snykTest"    | "jsonOutputPath"                 | osPath("/path/to/json1")             | "RegularFile"                     | property              | _          || _
        "snykReport"  | "jsonOutputPath"                 | osPath("/path/to/json2")             | "RegularFile"                     | property              | _          || _
        "snykMonitor" | "jsonOutputPath"                 | osPath("/path/to/json3")             | "RegularFile"                     | property              | _          || _
        "snykTest"    | "printSarif"                     | true                                 | "Boolean"                         | property              | _          || _
        "snykReport"  | "printSarif"                     | true                                 | "Boolean"                         | property              | _          || _
        "snykMonitor" | "printSarif"                     | true                                 | "Boolean"                         | property              | _          || _
        "snykTest"    | "sarifOutputPath"                | osPath("/path/to/sarif1")            | "RegularFile"                     | property              | _          || _
        "snykReport"  | "sarifOutputPath"                | osPath("/path/to/sarif2")            | "RegularFile"                     | property              | _          || _
        "snykMonitor" | "sarifOutputPath"                | osPath("/path/to/sarif3")            | "RegularFile"                     | property              | _          || _
        "snykTest"    | "severityThreshold"              | SeverityThresholdOption.critical     | "SeverityThresholdOption"         | property              | _          || _
        "snykReport"  | "severityThreshold"              | SeverityThresholdOption.critical     | "SeverityThresholdOption"         | property              | _          || _
        "snykMonitor" | "severityThreshold"              | SeverityThresholdOption.low          | "SeverityThresholdOption"         | property              | _          || _
        "snykTest"    | "failOn"                         | FailOnOption.all                     | "FailOnOption"                    | property              | _          || _
        "snykReport"  | "failOn"                         | FailOnOption.all                     | "FailOnOption"                    | property              | _          || _
        "snykMonitor" | "failOn"                         | FailOnOption.upgradable              | "FailOnOption"                    | property              | _          || _
        "snykTest"    | "compilerArguments"              | ['--flag1']                          | "List<String>"                    | property              | _          || _
        "snykReport"  | "compilerArguments"              | ['--flag2']                          | "List<String>"                    | property              | _          || _
        "snykMonitor" | "compilerArguments"              | ['--flag3']                          | "List<String>"                    | property              | _          || _

        "snykTest"    | "assetsProjectName"              | true                                 | "Boolean"                         | property              | _          || _
        "snykReport"  | "assetsProjectName"              | true                                 | "Boolean"                         | property              | _          || _
        "snykMonitor" | "assetsProjectName"              | true                                 | "Boolean"                         | property              | _          || _
        "snykTest"    | "packagesFolder"                 | osPath("/path/to/packages1")         | "Directory"                       | property              | _          || _
        "snykReport"  | "packagesFolder"                 | osPath("/path/to/packages2")         | "Directory"                       | property              | _          || _
        "snykMonitor" | "packagesFolder"                 | osPath("/path/to/packages3")         | "Directory"                       | property              | _          || _
        "snykTest"    | "projectNamePrefix"              | "prefix1"                            | "String"                          | property              | _          || _
        "snykReport"  | "projectNamePrefix"              | "prefix2"                            | "String"                          | property              | _          || _
        "snykMonitor" | "projectNamePrefix"              | "prefix3"                            | "String"                          | property              | _          || _

        "snykTest"    | "subProject"                     | "project1"                           | "String"                          | property              | _          || _
        "snykReport"  | "subProject"                     | "project2"                           | "String"                          | property              | _          || _
        "snykMonitor" | "subProject"                     | "project3"                           | "String"                          | property              | _          || _
        "snykTest"    | "allSubProjects"                 | true                                 | "Boolean"                         | property              | _          || _
        "snykReport"  | "allSubProjects"                 | true                                 | "Boolean"                         | property              | _          || _
        "snykMonitor" | "allSubProjects"                 | true                                 | "Boolean"                         | property              | _          || _
        "snykTest"    | "configurationMatching"          | "config1"                            | "String"                          | property              | _          || _
        "snykReport"  | "configurationMatching"          | "config2"                            | "String"                          | property              | _          || _
        "snykMonitor" | "configurationMatching"          | "config3"                            | "String"                          | property              | _          || _
        "snykTest"    | "configurationAttributes"        | ['attribute1']                       | "List<String>"                    | property              | _          || _
        "snykReport"  | "configurationAttributes"        | ['attribute2']                       | "List<String>"                    | property              | _          || _
        "snykMonitor" | "configurationAttributes"        | ['attribute3']                       | "List<String>"                    | property              | _          || _
        "snykTest"    | "reachable"                      | true                                 | "Boolean"                         | property              | _          || _
        "snykReport"  | "reachable"                      | true                                 | "Boolean"                         | property              | _          || _
        "snykMonitor" | "reachable"                      | true                                 | "Boolean"                         | property              | _          || _
        "snykTest"    | "reachableTimeout"               | 22                                   | "Integer"                         | property              | _          || _
        "snykReport"  | "reachableTimeout"               | 22                                   | "Integer"                         | property              | _          || _
        "snykMonitor" | "reachableTimeout"               | 45                                   | "Integer"                         | property              | _          || _
        "snykTest"    | "initScript"                     | osPath("/path/to/initScript1")       | "RegularFile"                     | property              | _          || _
        "snykReport"  | "initScript"                     | osPath("/path/to/initScript2")       | "RegularFile"                     | property              | _          || _
        "snykMonitor" | "initScript"                     | osPath("/path/to/initScript3")       | "RegularFile"                     | property              | _          || _

        "snykTest"    | "scanAllUnmanaged"               | true                                 | "Boolean"                         | property              | _          || _
        "snykReport"  | "scanAllUnmanaged"               | true                                 | "Boolean"                         | property              | _          || _
        "snykMonitor" | "scanAllUnmanaged"               | true                                 | "Boolean"                         | property              | _          || _

        "snykTest"    | "strictOutOfSync"                | true                                 | "Boolean"                         | property              | _          || _
        "snykReport"  | "strictOutOfSync"                | true                                 | "Boolean"                         | property              | _          || _
        "snykMonitor" | "strictOutOfSync"                | true                                 | "Boolean"                         | property              | _          || _

        "snykTest"    | "command"                        | "command1"                           | "String"                          | property              | _          || _
        "snykReport"  | "command"                        | "command2"                           | "String"                          | property              | _          || _
        "snykMonitor" | "command"                        | "command3"                           | "String"                          | property              | _          || _
        "snykTest"    | "skipUnresolved"                 | true                                 | "Boolean"                         | property              | _          || _
        "snykReport"  | "skipUnresolved"                 | true                                 | "Boolean"                         | property              | _          || _
        "snykMonitor" | "skipUnresolved"                 | true                                 | "Boolean"                         | property              | _          || _

        "snykTest"    | "insecure"                       | true                                 | "Boolean"                         | property              | _          || _
        "snykReport"  | "insecure"                       | true                                 | "Boolean"                         | property              | _          || _
        "snykMonitor" | "insecure"                       | true                                 | "Boolean"                         | property              | _          || _
        "snykTest"    | "debug"                          | true                                 | "Boolean"                         | property              | _          || _
        "snykReport"  | "debug"                          | true                                 | "Boolean"                         | property              | _          || _
        "snykMonitor" | "debug"                          | true                                 | "Boolean"                         | property              | _          || _

        "snykTest"    | "token"                          | "test_token_1"                       | "String"                          | property              | _          || _
        "snykReport"  | "token"                          | "test_token_2"                       | "String"                          | property              | _          || _
        "snykMonitor" | "token"                          | "test_token_3"                       | "String"                          | property              | _          || _

        "snykTest"    | "executableName"                 | "snyk1"                              | "String"                          | property              | _          || _
        "snykReport"  | "executableName"                 | "snyk2"                              | "String"                          | property              | _          || _
        "snykMonitor" | "executableName"                 | "snyk3"                              | "String"                          | property              | _          || _
        "snykTest"    | "snykPath"                       | osPath("/path/to/snyk_home_1")       | "Directory"                       | property              | _          || _
        "snykReport"  | "snykPath"                       | osPath("/path/to/snyk_home_2")       | "Directory"                       | property              | _          || _
        "snykMonitor" | "snykPath"                       | osPath("/path/to/snyk_home_3")       | "Directory"                       | property              | _          || _

        "snykMonitor" | "trustPolicies"                  | true                                 | "Boolean"                         | property              | _          || _
        "snykMonitor" | "projectEnvironment"             | [EnvironmentOption.onprem]           | "List<EnvironmentOption>"         | property              | _          || _
        "snykMonitor" | "projectLifecycle"               | [LifecycleOption.development]        | "List<LifecycleOption>"           | property              | _          || _
        "snykMonitor" | "projectBusinessCriticality"     | [BusinessCriticalityOption.critical] | "List<BusinessCriticalityOption>" | property              | _          || _
        "snykMonitor" | "projectTags"                    | ["foo": "bar"]                       | "Map<String, String>"             | property              | _          || _

        "snykTest"    | "reports.sarif.required"         | true                                 | "Boolean"                         | "sarifReportsEnabled" | _          || _
        "snykTest"    | "reports.json.required"          | true                                 | "Boolean"                         | "jsonReportsEnabled"  | _          || _
        "snykReport"  | "reports.sarif.required"         | true                                 | "Boolean"                         | "sarifReportsEnabled" | _          || _

        "snykTest"    | "debug"                          | true                                 | "Boolean"                         | property              | _          || _
        "snykMonitor" | "debug"                          | true                                 | "Boolean"                         | property              | _          || _
        "snykReport"  | "debug"                          | true                                 | "Boolean"                         | property              | _          || _

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
        "projectName" | customProjectNameValue | "String"      | null                    || _
        "subProject"  | customSubProjectValue  | "String"      | "someSubProjectName"    || _

        value = (type != _) ? wrapValueBasedOnType(rawValue, type.toString(), wrapValueFallback) : rawValue
        rootExtensionValue = (type != _ && rawRootExtensionValue) ? wrapValueBasedOnType(rawRootExtensionValue, type.toString(), wrapValueFallback) : rawRootExtensionValue
        testValue = (expectedValue == _) ? rawValue : expectedValue
    }

    @Unroll("projectName for registered project is based on #message")
    def "projectName for registered project"() {
        given: "a registered gradle sub project"
        setupProject()
        buildFile << """
        ${extensionName} {
            ${getRegisterProjectInvocation(packageId)}
            projectName = ${rootProjectNameValue}
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
                .replace("#projectName#", moduleName)
                .replace("#packageName#", packageId)
                .replace("#subProjectName#", packageId)
                .replace("#projectDir#", projectDir.absolutePath) : testValue
        query.matches(result, v)

        where:
        rootProjectName   || expectedSubProjectName
        "testProjectRoot" || "${rootProjectName}:#packageName#"
        null              || "#projectName#:#packageName#"

        property = "projectName"
        rootProjectNameValue = (rootProjectName) ? wrapValueBasedOnType(rootProjectName, String) : null
        testValue = expectedSubProjectName.toString()
        message = rootProjectName ? "root project 'projectName' property value if set" : "root project gradle project name when 'projectName' property is not set"
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
