package wooga.gradle.snyk.tasks

import com.wooga.gradle.PlatformUtils
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import wooga.gradle.snyk.SnykInstallSpec
import wooga.gradle.snyk.tasks.internal.CachedGroliphantDownloader
import wooga.gradle.snyk.tasks.internal.SnykDownloadAsset

abstract class SnykInstallBase extends DefaultTask implements SnykInstallSpec {

    static String getReleaseJsonUrl(String version) {
        def baseUrl = System.getProperty("static.snyk.io.baseUrl", "https://static.snyk.io")
        "${baseUrl}/cli/${version}/release.json"
    }

    @OutputFile
    abstract Provider<RegularFile> getSnykExecutable()

    @Internal
    abstract Provider<Map<String, SnykDownloadAsset>> getSnykReleasesProvider()

    @TaskAction
    void run() {
        def downloader = new CachedGroliphantDownloader(project)
        snykReleasesProvider.get()[platformSpecificFileName].download(snykExecutable.get().asFile, downloader)
    }

    @Internal
    protected abstract String getPlatformSpecificFileName()

    static ensureVersionPrefix(String versionTag, String prefix = "v") {
        if (versionTag != "latest" && !versionTag.startsWith(prefix)) {
            return prefix + versionTag
        }
        versionTag
    }

    static String normalizeExecutableByOS(String executableName) {
        if (PlatformUtils.isWindows()) {
            return executableName.endsWith(".exe") ? executableName : "${executableName}.exe"
        }
        return executableName
    }
}
