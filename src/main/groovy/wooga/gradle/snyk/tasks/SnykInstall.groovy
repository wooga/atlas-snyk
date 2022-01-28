package wooga.gradle.snyk.tasks

import com.wooga.gradle.PlatformUtils
import groovy.json.JsonSlurper
import org.gradle.api.DefaultTask
import org.gradle.api.file.Directory
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import wooga.gradle.snyk.tasks.internal.Downlader
import wooga.gradle.snyk.tasks.internal.CachedGroliphantDownloader
import wooga.gradle.snyk.tasks.internal.SnykDownloadAsset

class SnykInstall extends DefaultTask {

    private static final String SNYK_RELEASE_JSON_URL = "https://static.snyk.io/cli/%s/release.json"
    private final Property<File> installationDir
    private final Property<String> executableName
    private final Property<String> snykVersion

    private final Downlader downloader
    private final Property<File> snykExecProperty

    SnykInstall() {
        this.installationDir = project.objects.property(File)
        this.executableName = project.objects.property(String)
        this.snykVersion = project.objects.property(String)
        this.snykExecProperty = project.objects.property(File)

        this.downloader = new CachedGroliphantDownloader(project)
    }

    @TaskAction
    void run() {
        String snykVersion = this.snykVersion.get()
        Optional<String> executableName = Optional.ofNullable(this.executableName.getOrNull())
        File installationDir = this.installationDir.get()

        File finalInstallationDir = new File(installationDir, snykVersion)
        File snykExecutable = downloadRelease(finalInstallationDir, executableName, snykVersion)
        this.snykExecProperty.set(snykExecutable)
    }

    private File downloadRelease(File installationDir, Optional<String> executableName, String snykVersion) {
        def snykReleases = fetchSnykReleases(snykVersion)
        if(PlatformUtils.isWindows()) {
            def winExecutableName = executableName.map{it.endsWith(".exe")? it : "${it}.exe"}
            def executableFile = new File(installationDir, winExecutableName.orElse("snyk-win.exe"))
            return snykReleases."snyk-win.exe".download(executableFile, downloader)
        } else if(PlatformUtils.isMac()) {
            def executableFile = new File(installationDir, executableName.orElse("snyk-macos"))
            return snykReleases."snyk-macos".download(executableFile, downloader)
        } else if(PlatformUtils.isLinux()) {
            def executableFile = new File(installationDir, executableName.orElse("snyk-linux"))
            return snykReleases."snyk-linux".download(executableFile, downloader)
        }
        throw new UnsupportedOperationException("Current OS do not have an available snyk binary for download")
    }

    private static Map<String, SnykDownloadAsset> fetchSnykReleases(String versionTag) {
        def releasesURL = String.format(SNYK_RELEASE_JSON_URL, versionTag)
        def result = new JsonSlurper().parse(new URL(releasesURL)) as Map

        String actualVersion = result.version
        Map<String, Map> assets = result.assets as Map<String, Map>

        return assets.collectEntries {String name, Map assetData ->
            return [(name): new SnykDownloadAsset(name, actualVersion,
                                                new URL(assetData.url as String), assetData.sha256 as String)]
        } as Map<String, SnykDownloadAsset>
    }

    @Input
    Property<File> getInstallationDir() {
        return installationDir
    }

    @Input @org.gradle.api.tasks.Optional
    Property<String> getExecutableName() {
        return executableName
    }

    @Input
    Property<String> getSnykVersion() {
        return snykVersion
    }

//    @OutputFile
    Provider<File> getSynkExecutable() {
        return snykExecProperty
    }

    void setInstallationDir(File file) {
        installationDir.set(file)
    }

    void setInstallationDir(Provider<Directory> dirProvider) {
        installationDir.set(dirProvider.get().asFile)

    }

}
