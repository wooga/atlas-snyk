package wooga.gradle.snyk

import com.wooga.gradle.test.PropertyQueryTaskWriter
import com.wooga.gradle.test.PropertyUtils
import spock.lang.Unroll

class SnykPluginSubProjectRegistrationIntegrationSpec extends SnykPluginRegistrationIntegrationSpec {

    @Override
    String getPackageId() {
        "sub1"
    }

    @Override
    String getRegisterProjectInvocation(String projectId) {
        "registerProject(findProject(${wrapValueBasedOnType(projectId, String)}))"
    }

    @Override
    String getGeneratedExtensionName() {
        extensionName
    }

    @Override
    File getRegisteredBuildFile() {
        new File(projectDir, "${packageId}/build.gradle")
    }

    @Override
    void setupProject() {
        addSubproject(packageId, "")
    }

    @Override
    String getCustomPackageFileValue() {
        null
    }

    @Override
    String getCustomProjectNameValue() {
        "#projectName#:#subProjectName#"
    }

    File getCustomProjectPath() {
        new File(projectDir, packageId)
    }

    @Override
    String getCustomSubProjectValue() {
        "#subProjectName#"
    }

    @Override
    String getGeneratedTaskName(String baseTaskName) {
        baseTaskName
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
                .replace("#projectDir#", projectDir.absolutePath)
                .replace("#subProjectDir#", customProjectPath.absolutePath) : testValue
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
                .replace("#projectDir#", projectDir.absolutePath)
                .replace("#subProjectDir#", customProjectPath.absolutePath) : testValue
        !query.matches(result, v2)

        where:
        property     | rawValue              | type        | rawRootExtensionValue           || expectedValue
        "reportsDir" | customReportsDirValue | "Directory" | "build${File.separator}reports" || _

        value = (type != _) ? wrapValueBasedOnType(rawValue, type.toString(), wrapValueFallback) : rawValue
        rootExtensionValue = (type != _ && rawRootExtensionValue) ? wrapValueBasedOnType(rawRootExtensionValue, type.toString(), wrapValueFallback) : rawRootExtensionValue
        testValue = (expectedValue == _) ? rawValue : expectedValue
    }

    @Unroll
    def "can register and configure multiple sub projects with #method"() {
        given: "multiple sub projects"
        def sub1Dir = addSubproject("sub1", "")
        def sub1BuildFile = new File(sub1Dir, "build.gradle")
        def sub2Dir = addSubproject("sub2", "")
        def sub2BuildFile = new File(sub2Dir, "build.gradle")

        buildFile << """
        ${extensionName} {
            ${method} {
                ${property} = ${value}
            }
        }
        """.stripIndent()

        when:
        def query1 = new PropertyQueryTaskWriter("${extensionName}.${property}")
        query1.write(sub1BuildFile)
        def result = runTasksSuccessfully(query1.taskName)

        then:
        query1.matches(result, testValue)

        when:
        def query2 = new PropertyQueryTaskWriter("${extensionName}.${property}")
        query2.write(sub2BuildFile)
        result = runTasksSuccessfully(query2.taskName)

        then:
        query2.matches(result, testValue)

        where:
        method                                                                        | property | rawValue | type     || expectedValue
        "registerProject([project.findProject('sub1'), project.findProject('sub2')])" | "token"  | "123456" | "String" || _
        "registerAllSubProjects()"                                                    | "token"  | "123456" | "String" || _

        value = (type != _) ? wrapValueBasedOnType(rawValue, type.toString(), wrapValueFallback) : rawValue
        testValue = (expectedValue == _) ? rawValue : expectedValue
    }
}
