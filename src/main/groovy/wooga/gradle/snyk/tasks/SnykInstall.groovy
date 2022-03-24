package wooga.gradle.snyk.tasks

import com.wooga.gradle.PlatformUtils
import groovy.json.JsonSlurper
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import wooga.gradle.snyk.tasks.internal.SnykDownloadAsset

class SnykInstall extends SnykInstallBase {

    static String getReleaseJsonUrl(String version) {
        def baseUrl = System.getProperty("static.snyk.io.baseUrl", "https://static.snyk.io")
        "${baseUrl}/cli/${version}/release.json"
    }

    final Provider<RegularFile> snykExecutable
    final Provider<Map<String, SnykDownloadAsset>> snykReleasesProvider

    private final Provider<String> versionToInstall

    @Input
    Provider<String> getVersionToInstall() {
        versionToInstall
    }

    @Override
    protected String getPlatformSpecificFileName() {
        if (PlatformUtils.isWindows()) {
            return "snyk-win.exe"
        } else if (PlatformUtils.isMac()) {
            return "snyk-macos"
        } else if (PlatformUtils.isLinux()) {
            return "snyk-linux"
        }
        throw new UnsupportedOperationException("Current OS do not have an available snyk binary for download")
    }

    SnykInstall() {
        this.snykExecutable = installationDir.zip(executableName) {
            dir, fileName -> dir.file(normalizeExecutableByOS(fileName))
        }

        this.versionToInstall = version.map({
            def version = it
            if (version == "latest") {
                def releasesURL = getReleaseJsonUrl(version)
                def result = new JsonSlurper().parse(new URL(releasesURL)) as Map
                version = result.version
            } else if (version.startsWith("v")) {
                version = version.substring(1)
            }
            version
        }.memoize())

        this.snykReleasesProvider = versionToInstall.map({
            String version -> fetchSnykReleases(version)
        }.memoize())
    }

    private static Map<String, SnykDownloadAsset> fetchSnykReleases(String versionTag) {
        def releasesURL = getReleaseJsonUrl(ensureVersionPrefix(versionTag))
        def result = new JsonSlurper().parse(new URL(releasesURL)) as Map

        String actualVersion = result.version
        Map<String, Map> assets = result.assets as Map<String, Map>

        return assets.collectEntries { String name, Map assetData ->
            return [(name): new SnykDownloadAsset(name, actualVersion,
                    new URL(assetData.url as String), assetData.sha256 as String)]
        } as Map<String, SnykDownloadAsset>
    }
}
