package wooga.gradle.snyk.tasks

import com.wooga.gradle.test.PropertyQueryTaskWriter
import org.apache.commons.codec.digest.DigestUtils
import spock.lang.IgnoreIf
import spock.lang.Unroll
import wooga.gradle.snyk.SnykIntegrationSpec
import wooga.gradle.snyk.SnykPlugin

import static com.wooga.gradle.PlatformUtils.*
import static com.wooga.gradle.test.PropertyUtils.toProviderSet
import static com.wooga.gradle.test.PropertyUtils.toSetter

class SnykInstallIntegrationSpec extends SnykIntegrationSpec {

    /**Snyk cache for tests where the downloading process itself doesn't matter*/
    private static final File CACHED_SNYK_DIR = File.createTempDir("atlas-snyk", "installTest")

    String subjectUnderTestName = "installTest"
    String subjectUnderTestTypeName = SnykInstall.name

    def setup() {
        buildFile << """
            project.tasks.register("${subjectUnderTestName}", ${subjectUnderTestTypeName})
        """
    }

    def "install snyk using plugin defaults"() {
        given:
        def pluginInstallTask = "snykInstall"

        and: "snyk plugin applied with conventions"
        buildFile << """
            ${applyPlugin(SnykPlugin)}
        """.stripIndent()

        and: "a future snyk executable"
        def expectedSnykFile = new File("${getGradleUserHome()}/atlas-snyk/v1.840.0/${isWindows() ? "snyk.exe" : "snyk"}")
        if (expectedSnykFile.exists()) {
            expectedSnykFile.delete()
        }

        when:
        def result = runTasksSuccessfully(pluginInstallTask)

        then:
        !result.wasSkipped(pluginInstallTask)

        expectedSnykFile.file
        expectedSnykFile.exists()

        def process = Runtime.runtime.exec([expectedSnykFile.absolutePath, "--version"] as String[])
        process.waitFor() == 0
    }


    @IgnoreIf({ !isMac() })
    @Unroll("installs valid snyk file from version #version on target directory for macOS")
    def "installs given snyk version on target directory for macOS"() {
        given: "a snyk install task with specified parameters"
        appendToSubjectTask(buildGradleSnykInstallTask(version, new File(projectDir, targetDir), executableName))

        when:
        runTasksSuccessfully(subjectUnderTestName)

        then:
        def expectedSnykFile = new File(projectDir, expectedSnykExec.path)
        expectedSnykFile.file
        DigestUtils.sha256Hex(expectedSnykFile.bytes) == expectedChecksum
        def process = Runtime.runtime.exec([expectedSnykFile.absolutePath, "--version"] as String[])
        process.waitFor() == 0

        where:
        version    | targetDir | executableName | expectedSnykExec      | expectedChecksum
        "v1.839.0" | "snyk"    | null           | new File("snyk/snyk") | "f3e5e31def231eb04d910c3cf0723c922c858fa1e9d1f2d1cdbd9099be878897"
        "v1.838.0" | "snyk"    | "snyk"         | new File("snyk/snyk") | "33f65baa53a3d9f0b1406225ed215e802be94bd429c5141ef21cc5ce16daedb6"
        "v1.837.0" | "snuk"    | "snek"         | new File("snuk/snek") | "b467c382ea2ed0763db8e07b36a4724ff4c7b705587f616025aca3077d07ee95"
    }


    @IgnoreIf({ !isLinux() })
    @Unroll("installs valid snyk file from version #version on target directory for linux")
    def "installs given snyk version on target directory for linux"() {
        given: "a snyk install task with specified parameters"
        appendToSubjectTask(buildGradleSnykInstallTask(version, new File(projectDir, targetDir), executableName))

        when:
        runTasksSuccessfully(subjectUnderTestName)

        then:
        def expectedSnykFile = new File(projectDir, expectedSnykExec.path)
        expectedSnykFile.file
        DigestUtils.sha256Hex(expectedSnykFile.bytes) == expectedChecksum
        def process = Runtime.runtime.exec([expectedSnykFile.absolutePath, "--version"] as String[])
        process.waitFor() == 0

        where:
        version    | targetDir | executableName | expectedSnykExec      | expectedChecksum
        "v1.839.0" | "snyk"    | null           | new File("snyk/snyk") | "5cd779adbef19c3c9982f01b672d5bd0e0005943a9dbb7038f8be772f70d1ddb"
        "v1.838.0" | "snyk"    | "snuk"         | new File("snyk/snuk") | "c52363013e21413c65e6b839c5ff5355c3c4ffc12b168e9c365a63b0c09ea4c8"
        "v1.837.0" | "snuk"    | "snek"         | new File("snuk/snek") | "e9ff8be02250a7b0bd85e8bde8ecb7939eb79c08a1c8ab59b3d0f1ef65d006df"
    }

    @IgnoreIf({ !isWindows() })
    @Unroll("installs valid snyk file from version #version on target directory for windows")
    def "installs given snyk version on target directory for windows"() {
        given: "a snyk install task with specified parameters"
        appendToSubjectTask(buildGradleSnykInstallTask(version, new File(projectDir, targetDir), executableName))

        when:
        runTasksSuccessfully(subjectUnderTestName)

        then:
        def expectedSnykFile = new File(projectDir, expectedSnykExec.path)
        expectedSnykFile.file
        DigestUtils.sha256Hex(expectedSnykFile.bytes) == expectedChecksum
        def process = Runtime.runtime.exec([expectedSnykFile.absolutePath, "--version"] as String[])
        process.waitFor() == 0

        where:
        version    | targetDir | executableName | expectedSnykExec          | expectedChecksum
        "v1.839.0" | "snyk"    | null           | new File("snyk/snyk.exe") | "6559946fa8270ad498d58fb5e75d0c55d014a70ac59383ea8440baefe2b2be7d"
        "v1.838.0" | "snyk"    | "snuk"         | new File("snyk/snuk.exe") | "5178479ca737963048baf84bc151901d30047bee0e67b6d836766d22c981c016"
        "v1.837.0" | "snuk"    | "snek"         | new File("snuk/snek.exe") | "b2ae120eea9872843a1090a3b3f5e602ef7c185aa49b974834880bc376ed59a8"
    }

    def "doesn't redownload snyk if identical version already exists on installationDir"() {
        given: "a snyk install task"
        appendToSubjectTask(buildGradleSnykInstallTask("v1.839.0", CACHED_SNYK_DIR, "snyk"))

        and: "a already existing snyk"
        def targetSnykFile = new File(CACHED_SNYK_DIR, "${isWindows() ? "snyk.exe" : "snyk"}")
        if (!targetSnykFile.file) {
            runTasksSuccessfully(subjectUnderTestName)
        }
        assert targetSnykFile.file

        when:
        def originalCreationTS = targetSnykFile.lastModified()
        runTasksSuccessfully(subjectUnderTestName)

        then:
        def snykFile = new File(targetSnykFile.path)
        originalCreationTS == snykFile.lastModified()
    }

    @Unroll("can set property #property with cli option #cliOption")
    def "can set property via cli option"() {
        given: "a task to read back the value"
        def query = new PropertyQueryTaskWriter("${subjectUnderTestName}.${property}")
        query.write(buildFile)

        and: "set minimal needed properties"
        appendToSubjectTask("""
        installationDir = file('build/installs')
        executableName = 'snyk'
        version = '1.0.0'
        """.stripIndent())

        and: "tasks to execute"
        def tasks = [subjectUnderTestName, cliOption]
        if (rawValue != _) {
            tasks.add(value)
        }
        tasks.add(query.taskName)

        and: "disable subject under test to no fail"
        appendToSubjectTask("enabled=false")


        when:
        def result = runTasksSuccessfully(*tasks)

        then:
        query.matches(result, expectedValue)

        where:
        property          | cliOption            | rawValue                      | returnValue | type
        "installationDir" | "--installation-dir" | osPath("/path/to/installDir") | _           | "CLIString"
        "executableName"  | "--executable-name"  | "my-custom-snyk"              | _           | "CLIString"
        "version"         | "--version"          | "22.11.33"                    | _           | "CLIString"

        value = wrapValueBasedOnType(rawValue, type, wrapValueFallback)
        expectedValue = returnValue == _ ? rawValue : returnValue
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
        property          | method                  | rawValue                       | returnValue | type
        "executableName"  | toProviderSet(property) | "snyk1"                        | _           | "String"
        "executableName"  | toProviderSet(property) | "snyk2"                        | _           | "Provider<String>"
        "executableName"  | toSetter(property)      | "snyk3"                        | _           | "String"
        "executableName"  | toSetter(property)      | "snyk4"                        | _           | "Provider<String>"

        "version"         | toProviderSet(property) | "11.22.1"                      | _           | "String"
        "version"         | toProviderSet(property) | "11.22.2"                      | _           | "Provider<String>"
        "version"         | toSetter(property)      | "11.22.3"                      | _           | "String"
        "version"         | toSetter(property)      | "11.22.4"                      | _           | "Provider<String>"

        "installationDir" | toProviderSet(property) | osPath("/path/to/installDir1") | _           | "File"
        "installationDir" | toProviderSet(property) | osPath("/path/to/installDir2") | _           | "Provider<Directory>"
        "installationDir" | toSetter(property)      | osPath("/path/to/installDir3") | _           | "File"
        "installationDir" | toSetter(property)      | osPath("/path/to/installDir4") | _           | "Provider<Directory>"
        "installationDir" | property                | osPath("/path/to/installDir5") | _           | "String"

        value = wrapValueBasedOnType(rawValue, type, wrapValueFallback)
        expectedValue = returnValue == _ ? rawValue : returnValue
    }

    @Unroll
    def "help prints commandline flag '#commandlineFlag' description"() {
        when:
        def result = runTasksSuccessfully("help", "--task", subjectUnderTestName)

        then:
        result.standardOutput.contains("Description")
        result.standardOutput.contains("Group")

        result.standardOutput.contains(commandlineFlag)

        where:
        commandlineFlag      | _
        "--executable-name"  | _
        "--installation-dir" | _
        "--version"          | _
    }

    def buildGradleSnykInstallTask(String snykVersion, File installationDir, String executableName = null) {
        return """
            version = "${snykVersion}"
            installationDir = ${wrapValueBasedOnType(installationDir, File)}
            executableName = ${wrapValueBasedOnType(executableName ?: "snyk", String)}
        """
    }

    static File getGradleUserHome() {
        def gradleUserHomeEnv = System.getenv("GRADLE_USER_HOME")
        if (!gradleUserHomeEnv) {
            gradleUserHomeEnv = "${getUnixUserHomePath()}/.gradle"
        }
        new File(gradleUserHomeEnv)
    }
}
