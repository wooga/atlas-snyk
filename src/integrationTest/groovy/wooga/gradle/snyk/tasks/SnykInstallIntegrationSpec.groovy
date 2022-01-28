package wooga.gradle.snyk.tasks

import com.wooga.gradle.test.IntegrationSpec
import org.apache.commons.codec.digest.DigestUtils
import spock.lang.IgnoreIf
import spock.lang.Unroll
import wooga.gradle.snyk.SnykPlugin

import static com.wooga.gradle.PlatformUtils.isLinux
import static com.wooga.gradle.PlatformUtils.isMac
import static com.wooga.gradle.PlatformUtils.isWindows

class SnykInstallIntegrationSpec extends IntegrationSpec {

    /**Snyk cache for tests where the downloading process itself doesn't matter*/
    private static final String CACHED_SNYK_DIR = System.getProperty("java.io.tmpdir") + "/atlas-snyk-test"
    //system tmp dir

    def setup() {
        buildFile << """
            ${applyPlugin(SnykPlugin)}
        """
    }

    @IgnoreIf({ !isMac() })
    @Unroll("installs valid snyk file from version #version on target directory for macOS")
    def "installs given snyk version on target directory for macOS"() {
        given: "a snyk install task with specified parameters"
        buildFile << buildGradleSnykInstallTask(version, new File(projectDir, targetDir), executableName)

        when:
        runTasksSuccessfully("snykInstall")

        then:
        def expectedSnykFile = new File(projectDir, expectedSnykExec.path)
        expectedSnykFile.file
        DigestUtils.sha256Hex(expectedSnykFile.bytes) == expectedChecksum
        def process = Runtime.runtime.exec([expectedSnykFile.absolutePath, "--version"] as String[])
        process.waitFor() == 0

        where:
        version    | targetDir | executableName | expectedSnykExec                     | expectedChecksum
        "v1.839.0" | "snyk"    | null           | new File("snyk/v1.839.0/snyk-macos") | "f3e5e31def231eb04d910c3cf0723c922c858fa1e9d1f2d1cdbd9099be878897"
        "v1.838.0" | "snyk"    | "snyk"         | new File("snyk/v1.838.0/snyk")       | "33f65baa53a3d9f0b1406225ed215e802be94bd429c5141ef21cc5ce16daedb6"
        "v1.837.0" | "snuk"    | "snek"         | new File("snuk/v1.837.0/snek")       | "b467c382ea2ed0763db8e07b36a4724ff4c7b705587f616025aca3077d07ee95"
    }


    @IgnoreIf({ !isLinux() })
    @Unroll("installs valid snyk file from version #version on target directory for linux")
    def "installs given snyk version on target directory for linux"() {
        given: "a snyk install task with specified parameters"
        buildFile << buildGradleSnykInstallTask(version, new File(projectDir, targetDir), executableName)

        when:
        runTasksSuccessfully("snykInstall")

        then:
        def expectedSnykFile = new File(projectDir, expectedSnykExec.path)
        expectedSnykFile.file
        DigestUtils.sha256Hex(expectedSnykFile.bytes) == expectedChecksum
        def process = Runtime.runtime.exec([expectedSnykFile.absolutePath, "--version"] as String[])
        process.waitFor() == 0

        where:
        version    | targetDir | executableName | expectedSnykExec                     | expectedChecksum
        "v1.839.0" | "snyk"    | null           | new File("snyk/v1.839.0/snyk-linux") | "5cd779adbef19c3c9982f01b672d5bd0e0005943a9dbb7038f8be772f70d1ddb"
        "v1.838.0" | "snyk"    | "snyk"         | new File("snyk/v1.838.0/snyk")       | "c52363013e21413c65e6b839c5ff5355c3c4ffc12b168e9c365a63b0c09ea4c8"
        "v1.837.0" | "snuk"    | "snek"         | new File("snuk/v1.837.0/snek")       | "e9ff8be02250a7b0bd85e8bde8ecb7939eb79c08a1c8ab59b3d0f1ef65d006df"
    }

    @IgnoreIf({ !isWindows() })
    @Unroll("installs valid snyk file from version #version on target directory for windows")
    def "installs given snyk version on target directory for windows"() {
        given: "a snyk install task with specified parameters"
        buildFile << buildGradleSnykInstallTask(version, new File(projectDir, targetDir), executableName)

        when:
        runTasksSuccessfully("snykInstall")

        then:
        def expectedSnykFile = new File(projectDir, expectedSnykExec.path)
        expectedSnykFile.file
        DigestUtils.sha256Hex(expectedSnykFile.bytes) == expectedChecksum
        def process = Runtime.runtime.exec([expectedSnykFile.absolutePath, "--version"] as String[])
        process.waitFor() == 0

        where:
        version    | targetDir | executableName | expectedSnykExec                       | expectedChecksum
        "v1.839.0" | "snyk"    | null           | new File("snyk/v1.839.0/snyk-win.exe") | "6559946fa8270ad498d58fb5e75d0c55d014a70ac59383ea8440baefe2b2be7d"
        "v1.838.0" | "snyk"    | "snyk"         | new File("snyk/v1.838.0/snyk.exe")     | "5178479ca737963048baf84bc151901d30047bee0e67b6d836766d22c981c016"
        "v1.837.0" | "snuk"    | "snek"         | new File("snuk/v1.837.0/snek,exe")     | "b2ae120eea9872843a1090a3b3f5e602ef7c185aa49b974834880bc376ed59a8"
    }

    def "doesn't redownload snyk if identical version already exists on installationDir"() {
        given: "a snyk install task"
        buildFile << buildGradleSnykInstallTask("v1.839.0", new File(CACHED_SNYK_DIR), "snyk")

        and: "a already existing snyk"
        def targetSnykFile = new File("$CACHED_SNYK_DIR/v1.839.0/${isWindows() ? "snyk.exe" : "snyk"}")
        if (!targetSnykFile.file) {
            runTasksSuccessfully("snykInstall")
        }
        assert targetSnykFile.file

        when:
        def originalCreationTS = targetSnykFile.lastModified()
        runTasksSuccessfully("snykInstall")

        then:
        def snykFile = new File(targetSnykFile.path)
        originalCreationTS == snykFile.lastModified()
    }

    def buildGradleSnykInstallTask(String snykVersion, File installationDir, String executableName = null) {
        return """
        project.tasks.register("snykInstall", ${SnykInstall.name}) { it ->
            it.snykVersion = "${snykVersion}"
            it.installationDir = new File("${installationDir.path}")
            it.executableName = ${executableName ? "\"${executableName}\"" : "null"}
        }
        """
    }
}
