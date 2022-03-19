package wooga.gradle.snyk.tasks


import wooga.gradle.snyk.SnykPlugin

import static com.wooga.gradle.PlatformUtils.*
import static wooga.gradle.snyk.tasks.SnykInstallBaseIntegrationSpec.InstallTestModel.model

class SnykInstallIntegrationSpec extends SnykInstallBaseIntegrationSpec<SnykInstall> {

    List<InstallTestModel> getInstallTestVersions() {
        List<InstallTestModel> data = []
        data << model("1.839.0", "snyk", "snyk", new File("snyk/snyk"))
        data << model("1.838.0", "snyk", "snyk", new File("snyk/snyk"))
        data << model("1.837.0", "snuk", "snek", new File("snuk/snek"))

        if (isMac()) {
            data[0].expectedChecksum = "f3e5e31def231eb04d910c3cf0723c922c858fa1e9d1f2d1cdbd9099be878897"
            data[1].expectedChecksum = "33f65baa53a3d9f0b1406225ed215e802be94bd429c5141ef21cc5ce16daedb6"
            data[2].expectedChecksum = "b467c382ea2ed0763db8e07b36a4724ff4c7b705587f616025aca3077d07ee95"
        }
        if (isWindows()) {
            data[0].expectedChecksum = "6559946fa8270ad498d58fb5e75d0c55d014a70ac59383ea8440baefe2b2be7d"
            data[1].expectedChecksum = "5178479ca737963048baf84bc151901d30047bee0e67b6d836766d22c981c016"
            data[2].expectedChecksum = "b2ae120eea9872843a1090a3b3f5e602ef7c185aa49b974834880bc376ed59a8"
        }

        if (isLinux()) {
            data[0].expectedChecksum = "5cd779adbef19c3c9982f01b672d5bd0e0005943a9dbb7038f8be772f70d1ddb"
            data[1].expectedChecksum = "c52363013e21413c65e6b839c5ff5355c3c4ffc12b168e9c365a63b0c09ea4c8"
            data[2].expectedChecksum = "e9ff8be02250a7b0bd85e8bde8ecb7939eb79c08a1c8ab59b3d0f1ef65d006df"
        }
        data
    }

    def "install snyk using plugin defaults"() {
        given:
        def pluginInstallTask = "snykInstall"

        and: "snyk plugin applied with conventions"
        buildFile << """
            ${applyPlugin(SnykPlugin)}
        """.stripIndent()

        and: "a future snyk executable"
        File expectedSnykFile = new File("${getGradleUserHome()}/atlas-snyk/latest/${isWindows() ? "snyk.exe" : "snyk"}")
        if (expectedSnykFile.exists()) {
            expectedSnykFile.delete()
        }

        when:
        def result = runTasksSuccessfully(pluginInstallTask)

        then:
        !result.wasSkipped(pluginInstallTask)

        expectedSnykFile.file
        expectedSnykFile.exists()
        expectedSnykFile.canExecute()

        def process = Runtime.runtime.exec([expectedSnykFile.absolutePath, "--version"] as String[])
        process.waitFor() == 0
    }

    @Override
    String downloadRequestPath(String version, String file) {
        "/cli/${ensureVersionPrefix(version)}/${file}"
    }

    @Override
    List<String> getVersionFiles() {
        ["release.json"]
    }

    static File getGradleUserHome() {
        def gradleUserHomeEnv = System.getenv("GRADLE_USER_HOME")
        if (!gradleUserHomeEnv) {
            gradleUserHomeEnv = "${getUnixUserHomePath()}/.gradle"
        }
        new File(gradleUserHomeEnv)
    }

    static String fetchVersion(File executable) {
        if (executable.exists() && executable.canExecute()) {
            def stdErr = new ByteArrayOutputStream()
            def stdOut = new ByteArrayOutputStream()
            "${executable} --version".execute().waitForProcessOutput(stdOut, stdErr)

            return stdOut.toString().split(' ').first().trim()
        }
        null
    }
}
