package wooga.gradle.snyk.tasks

import com.wooga.gradle.PlatformUtils
import groovy.json.JsonSlurper
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import wooga.gradle.snyk.SnykInstallSpec
import wooga.gradle.snyk.tasks.internal.CachedGroliphantDownloader
import wooga.gradle.snyk.tasks.internal.Downlader
import wooga.gradle.snyk.tasks.internal.SnykDownloadAsset

class SnykInstall extends DefaultTask implements SnykInstallSpec {

    private static final String SNYK_RELEASE_JSON_URL = "https://static.snyk.io/cli/%s/release.json"

    @OutputFile
    final Provider<RegularFile> snykExecutable

    private final Provider<Map<String, SnykDownloadAsset>> snykReleasesProvider

    SnykInstall() {
        this.snykReleasesProvider =  version.map ({
            String version -> fetchSnykReleases(version)
        }.memoize())

        this.snykExecutable = installationDir.zip(executableName) {
            dir, fileName -> dir.file(normalizeExecutableByOS(fileName))
        }
    }

    @TaskAction
    void run() {
        def downloader = new CachedGroliphantDownloader(project)
        downloadRelease(snykExecutable.get().asFile, snykReleasesProvider.get(), downloader)
    }

    private static File downloadRelease(File fullExecutablePath, Map<String, SnykDownloadAsset> snykReleases, Downlader downloader) {
        if(PlatformUtils.isWindows()) {
            return snykReleases["snyk-win.exe"].download(fullExecutablePath, downloader)
        } else if(PlatformUtils.isMac()) {
            return snykReleases["snyk-macos"].download(fullExecutablePath, downloader)
        } else if(PlatformUtils.isLinux()) {
            return snykReleases["snyk-linux"].download(fullExecutablePath, downloader)
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

    private static String normalizeExecutableByOS(String executableName) {
        if(PlatformUtils.isWindows()) {
            return executableName.endsWith(".exe")? executableName : "${executableName}.exe"
        }
        return executableName
    }
}
