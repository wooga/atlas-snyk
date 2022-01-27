package wooga.gradle.snyk.tasks

import com.wooga.gradle.test.IntegrationSpec
import org.apache.commons.codec.digest.DigestUtils
import spock.lang.Unroll
import wooga.gradle.snyk.SnykPlugin

class SnykInstallIntegrationSpec extends IntegrationSpec {

    /**Snyk cache for tests where the downloading process itself doesn't matter*/
    private static final String CACHED_SNYK_DIR = System.getProperty("java.io.tmpdir") + "/snyk-test" //system tmp dir

    def setup() {
        buildFile << """
            ${applyPlugin(SnykPlugin)}
        """
    }

    @Unroll
    def "installs given snyk version #version on target directory for macOS"() {
        given: "a snyk install task with specified parameters"
        buildFile << """
        project.tasks.register("snykInstall", ${SnykInstall.name}) { it ->
            it.snykVersion = "${version}"
            it.installationDir = new File(projectDir, "${targetDir}")
        }
        """
        when:
        runTasksSuccessfully("snykInstall")
        then:
        def expectedSnykFile = new File(projectDir, expectedSnykExec.path)
        expectedSnykFile.file
        DigestUtils.sha256Hex(expectedSnykFile.bytes) == expectedChecksum
        //TODO: run snyk file to see if its able to execute (--version / -v)

        where:
        version | targetDir | expectedSnykExec | expectedChecksum
        "v1.839.0" | "snyk"   | new File("snyk/v1.839.0/snyk-macos") | "f3e5e31def231eb04d910c3cf0723c922c858fa1e9d1f2d1cdbd9099be878897"
        "v1.838.0" | "snyk"   | new File("snyk/v1.838.0/snyk-macos") | "33f65baa53a3d9f0b1406225ed215e802be94bd429c5141ef21cc5ce16daedb6"
        "v1.837.0" | "snuk"   | new File("snuk/v1.837.0/snyk-macos") | "b467c382ea2ed0763db8e07b36a4724ff4c7b705587f616025aca3077d07ee95"
    }

    def "installed snyk file is able to be executed"() {
        given: "a snyk install task"
        buildFile << """
        project.tasks.register("snykInstall", ${SnykInstall.name}) { it ->
            it.snykVersion = "v1.839.0"
            it.installationDir = new File("$CACHED_SNYK_DIR")
        }
        """
        when:
        runTasksSuccessfully("snykInstall")

        then:
        def snykFile = new File( "$CACHED_SNYK_DIR/v1.839.0/snyk-macos")
        def process = Runtime.runtime.exec([snykFile.absolutePath, "--version"] as String[])
        process.waitFor() == 0
    }

    def "doesn't redownload snyk if identical version already exists on installationDir"() {
        given: "a snyk install task"
        buildFile << """
        project.tasks.register("snykInstall", ${SnykInstall.name}) { it ->
            it.snykVersion = "v1.839.0"
            it.installationDir = new File("$CACHED_SNYK_DIR")
        }
        """
        and: "a already existing snyk"
        def targetSnykFile = new File( "$CACHED_SNYK_DIR/v1.839.0/snyk-macos")
        if(!targetSnykFile.file) {
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

    //TODO: custom download destination
    //TODO: platform-indepentent test target names (needs custom download destination)
}
