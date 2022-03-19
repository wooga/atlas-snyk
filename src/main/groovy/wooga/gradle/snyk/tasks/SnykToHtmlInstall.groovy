package wooga.gradle.snyk.tasks

import com.wooga.gradle.PlatformUtils
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import wooga.gradle.snyk.tasks.internal.SnykDownloadAsset

class SnykToHtmlInstall extends SnykInstallBase {

    static String getDownloadUrl(String version, String file) {
        if (version != 'latest') {
            version = ensureVersionPrefix(version)
        }
        def baseUrl = System.getProperty("static.snyk.io.baseUrl", "https://static.snyk.io")
        "${baseUrl}/snyk-to-html/${version}/${file}"
    }

    @OutputFile
    final Provider<RegularFile> snykExecutable
    final Provider<Map<String, SnykDownloadAsset>> snykReleasesProvider

    private final Provider<String> versionChecksum

    @Input
    Provider<String> getVersionChecksum() {
        versionChecksum
    }

    SnykToHtmlInstall() {
        this.snykExecutable = installationDir.zip(executableName) {
            dir, fileName -> dir.file(normalizeExecutableByOS(fileName))
        }

        this.versionChecksum = version.map({String version ->
            def checksumURL = new URL(getDownloadUrl(version, platformSpecificFileName + '.sha256'))
            def checksumOutput = new ByteArrayOutputStream()
            def checksum = checksumURL.withInputStream { i -> checksumOutput << i }.toString()
            checksum.trim().split(" ")[0]
        })

        this.snykReleasesProvider = version.zip(versionChecksum, { String version, String checksum ->
            [(platformSpecificFileName): new SnykDownloadAsset(
                    platformSpecificFileName,
                    version,
                    new URL(getDownloadUrl(version, platformSpecificFileName)),
                    checksum
            )]
        }.memoize())
    }

    @Override
    protected String getPlatformSpecificFileName() {
        if (PlatformUtils.isWindows()) {
            return "snyk-to-html-win.exe"
        } else if (PlatformUtils.isMac()) {
            return "snyk-to-html-macos"
        } else if (PlatformUtils.isLinux()) {
            return "snyk-to-html-linux"
        }
        throw new UnsupportedOperationException("Current OS do not have an available snyk-to-html binary for download")
    }
}
