package wooga.gradle.snyk

class SnykPluginPackeFileRegistrationIntegrationSpec extends SnykPluginRegistrationIntegrationSpec {

    @Override
    String getRegisterProjectInvocation(String projectId) {
        "registerProject(file(${wrapValueBasedOnType(projectId, String)}))"
    }

    @Override
    String getPackageId() {
        "somePackage.json"
    }

    @Override
    String getGeneratedTaskName(String baseTaskName) {
        baseTaskName + "." + packageId
    }

    @Override
    String getGeneratedExtensionName() {
        extensionName + "." + packageId
    }

    @Override
    File getRegisteredBuildFile() {
        buildFile
    }

    @Override
    void setupProject() {
        createFile(packageId)
    }

    @Override
    String getCustomPackageFileValue() {
        "#projectDir#${File.separator}#packageName#"
    }

    @Override
    String getCustomProjectNameValue() {
        "#projectName#:#packageName#"
    }

    @Override
    String getCustomSubProjectValue() {
        null
    }
}
