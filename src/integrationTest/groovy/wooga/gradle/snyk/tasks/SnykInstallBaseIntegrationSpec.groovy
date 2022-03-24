package wooga.gradle.snyk.tasks

import com.wooga.gradle.test.PropertyQueryTaskWriter
import org.apache.commons.codec.digest.DigestUtils
import org.mockserver.integration.ClientAndServer
import org.mockserver.logging.MockServerLogger
import org.mockserver.socket.tls.KeyStoreFactory
import spock.lang.Unroll
import spock.util.environment.RestoreSystemProperties
import wooga.gradle.snyk.SnykInstallSpec
import wooga.gradle.snyk.SnykIntegrationSpec

import javax.net.ssl.HttpsURLConnection
import java.lang.reflect.ParameterizedType
import java.nio.file.Files

import static com.wooga.gradle.PlatformUtils.windows
import static com.wooga.gradle.test.PropertyUtils.toProviderSet
import static com.wooga.gradle.test.PropertyUtils.toSetter
import static org.mockserver.integration.ClientAndServer.startClientAndServer
import static org.mockserver.model.HttpOverrideForwardedRequest.forwardOverriddenRequest
import static org.mockserver.model.HttpRequest.request
import static org.mockserver.stop.Stop.stopQuietly

abstract class SnykInstallBaseIntegrationSpec<T extends SnykInstallSpec> extends SnykIntegrationSpec {

    /**Snyk cache for tests where the downloading process itself doesn't matter*/
    private static final File CACHED_SNYK_DIR = File.createTempDir("atlas-snyk", "installTest")

    Class<T> getSubjectUnderTestClass() {
        if (!_sutClass) {
            try {
                this._sutClass = (Class<T>) ((ParameterizedType) this.getClass().getGenericSuperclass())
                        .getActualTypeArguments()[0];
            }
            catch (Exception e) {
                this._sutClass = (Class<T>) SnykTask
            }
        }
        _sutClass
    }
    private Class<T> _sutClass

    @Override
    String getSubjectUnderTestName() {
        "${subjectUnderTestClass.simpleName.uncapitalize()}Test"
    }

    @Override
    String getSubjectUnderTestTypeName() {
        subjectUnderTestClass.getTypeName()
    }

    def setup() {
        environmentVariables.set("SNYK_TOKEN", System.getenv("ATLAS_SNYK_INTEGRATION_TOKEN"))
        buildFile << """
        
        task $subjectUnderTestName(type: ${subjectUnderTestTypeName})
        """.stripIndent()
    }

    abstract List<InstallTestModel> getInstallTestVersions()

    @Unroll("installs valid snyk executable file for version #version to target directory")
    def "installs given snyk version on target directory for macOS"() {
        given: "a snyk install task with specified parameters"
        appendToSubjectTask(buildGradleSnykInstallTask(version, new File(projectDir, targetDir), executableName ?: "snyk"))

        when:
        runTasksSuccessfully(subjectUnderTestName)

        then:
        def expectedSnykFile = new File(projectDir, expectedSnykExec.path)
        expectedSnykFile.file
        DigestUtils.sha256Hex(expectedSnykFile.bytes) == expectedChecksum
        def process = Runtime.runtime.exec([expectedSnykFile.absolutePath, "--help"] as String[])
        process.waitFor() == 0

        where:
        version << installTestVersions.collect { it.version }
        targetDir << installTestVersions.collect { it.targetDir }
        executableName << installTestVersions.collect { it.executableName }
        expectedSnykExec << installTestVersions.collect { it.expectedSnykExec }
        expectedChecksum << installTestVersions.collect { it.expectedChecksum }
    }

    def "doesn't redownload executable if identical version already exists on installationDir"() {
        given: "a snyk install task"
        appendToSubjectTask(buildGradleSnykInstallTask(version, CACHED_SNYK_DIR, executeableName))

        and: "a already existing snyk"
        def targetSnykFile = new File(CACHED_SNYK_DIR, "${isWindows() ? "${executeableName}.exe" : executeableName}")
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

        where:
        version = installTestVersions[0].version
        executeableName = installTestVersions[0].executableName
    }

    def "task is up-to-date when latest fetches the same version"() {
        given: "a snyk install task with specified parameters"
        appendToSubjectTask(buildGradleSnykInstallTask("latest", new File(projectDir, "snyk"), "snyk"))

        when:
        def firstRunResult = runTasksSuccessfully(subjectUnderTestName)

        then:
        firstRunResult.wasExecuted(subjectUnderTestName)
        !firstRunResult.wasUpToDate(subjectUnderTestName)
        !firstRunResult.wasSkipped(subjectUnderTestName)

        when:
        def secondRunResult = runTasksSuccessfully(subjectUnderTestName)
        then:
        secondRunResult.wasExecuted(subjectUnderTestName)
        secondRunResult.wasUpToDate(subjectUnderTestName)
        !secondRunResult.wasSkipped(subjectUnderTestName)
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

    static class InstallTestModel {
        String version
        String targetDir
        String executableName
        File expectedSnykExec
        String expectedChecksum

        InstallTestModel(String version, String targetDir, String executableName, File expectedSnykExec, String expectedChecksum) {
            this.version = version
            this.targetDir = targetDir
            this.executableName = executableName
            if (isWindows() && !expectedSnykExec.name.endsWith(".exe")) {
                this.expectedSnykExec = new File(expectedSnykExec.path + ".exe")
            } else {
                this.expectedSnykExec = expectedSnykExec
            }
            this.expectedChecksum = expectedChecksum
        }

        static InstallTestModel model(String version, String targetDir, String executableName, File expectedSnykExec, String expectedChecksum) {
            new InstallTestModel(version, targetDir, executableName, expectedSnykExec, expectedChecksum)
        }

        static InstallTestModel model(String version, String targetDir, String executableName, File expectedSnykExec) {
            new InstallTestModel(version, targetDir, executableName, expectedSnykExec, null)
        }

    }

    protected void forwardAllSnykRequests(ClientAndServer mockServer) {
        mockServer
                .when(
                        request()
                )
                .forward(
                        forwardOverriddenRequest(
                                request()
                                        .withHeader("Host", "static.snyk.io")
                        )
                )
    }

    abstract String downloadRequestPath(String version, String file)

    void mockVersionRequests(ClientAndServer mockServer, String version, String mockVersion, List<String> files) {
        files.each { String file ->
            mockServer
                    .when(
                            request()
                                    .withPath(downloadRequestPath(version, file))
                    )
                    .forward(
                            forwardOverriddenRequest(
                                    request()
                                            .withPath(downloadRequestPath(mockVersion, file))
                                            .withHeader("Host", "static.snyk.io")
                            )
                    )
        }
    }

    void clearMockVersionRequests(ClientAndServer mockServer, String version, List<String> files) {
        files.each { String file ->
            mockServer.clear(request().withPath(downloadRequestPath(version, file)))
        }
    }

    abstract List<String> getVersionFiles()

    static ensureVersionPrefix(String versionTag, String prefix = "v") {
        if (versionTag != "latest" && !versionTag.startsWith(prefix)) {
            return prefix + versionTag
        }
        versionTag
    }

    @RestoreSystemProperties
    def "auto updates preinstalled version when version is set to 'latest'"() {
        given: "a snyk install task with specified parameters"
        appendToSubjectTask(buildGradleSnykInstallTask(version, new File(projectDir, installDir), executableName))

        and: "a proxy server to mock a specific result"
        // ensure all connection using HTTPS will use the SSL context defined by
        // MockServer to allow dynamically generated certificates to be accepted
        HttpsURLConnection.setDefaultSSLSocketFactory(new KeyStoreFactory(new MockServerLogger()).sslContext().getSocketFactory());
        def mockServer = startClientAndServer()

        System.setProperty("static.snyk.io.baseUrl", "https://localhost:${mockServer.getPort()}")
        mockServer.reset()

        and: "forward latest call to old release json"
        mockVersionRequests(mockServer, version, mockVersion, files)
        and: "a proxy setup for any path not /cli/latest/release.json"
        forwardAllSnykRequests(mockServer)
        //Opens a webpage with logs and other useful information about the current proxy/mock server
        //mockServer.openUI()

        and: "a future snyk file"
        def expectedSnykFile = new File(projectDir, "${installDir}/${normalizedExecutableName}")
        assert !expectedSnykFile.exists()

        when: "a first run to install snyk"
        runTasksSuccessfully(subjectUnderTestName)

        then:
        expectedSnykFile.exists()
        DigestUtils.sha256Hex(Files.newInputStream(expectedSnykFile.toPath())) == mockChecksum
        "${expectedSnykFile.absolutePath} --help".execute().waitFor() == 0


        when: "latest returns latest version"
        //clear the forward
        clearMockVersionRequests(mockServer, version, files)
        mockServer.clear(request())
        forwardAllSnykRequests(mockServer)

        def result = runTasksSuccessfully(subjectUnderTestName)

        then:
        result.wasExecuted(subjectUnderTestName)
        !result.wasUpToDate(subjectUnderTestName)
        expectedSnykFile.file
        expectedSnykFile.canExecute()
        DigestUtils.sha256Hex(Files.newInputStream(expectedSnykFile.toPath())) != mockChecksum
        "${expectedSnykFile.absolutePath} --help".execute().waitFor() == 0

        cleanup:
        stopQuietly(mockServer)

        where:
        version = 'latest'
        files = versionFiles
        mockVersion = installTestVersions.last().version
        mockChecksum = installTestVersions.last().expectedChecksum
        executableName = "snykExecutable"
        normalizedExecutableName = isWindows() ? "${executableName}.exe" : executableName
        installDir = "build/snyk"
    }
}
