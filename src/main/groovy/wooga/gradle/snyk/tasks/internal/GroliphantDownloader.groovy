package wooga.gradle.snyk.tasks.internal

import org.apache.commons.codec.digest.DigestUtils
import org.gradle.api.Project
import org.gradle.api.file.CopySpec
import org.gradle.wrapper.IDownload
import org.ysb33r.grolifant.api.core.CheckSumVerification
import org.ysb33r.grolifant.api.errors.ChecksumFailedException
import org.ysb33r.grolifant.api.v4.downloader.ArtifactDownloader
import org.ysb33r.grolifant.api.v4.downloader.ArtifactRootVerification
import org.ysb33r.grolifant.internal.v4.downloader.Downloader

class ChecksumVerifier implements CheckSumVerification {

    private final String checksum
    private final Closure verify

    ChecksumVerifier(String checksum, Closure verify) {
        this.checksum = checksum
        this.verify = verify
    }

    @Override
    void verify(File downloadedTarget) {
        this.verify.call(downloadedTarget)
    }

    @Override
    String getChecksum() {
        return checksum
    }
}

class GroliphantDownloader implements Downlader {

    private final Project project
    private final Closure<Downloader> downloaderFactory

    GroliphantDownloader(Project project) {
        this.project = project
        this.downloaderFactory = {String distName -> Downloader.create(distName, project.gradle.startParameter.logLevel)}
    }

    @Override
    File download(File fullPath, URL url, String sha256 = null) {
        IDownload downloader = downloaderFactory(fullPath.name)
        downloader.download(url.toURI(), fullPath)
//        CheckSumVerification verifier = new ChecksumVerifier(sha256, { File it ->
//            def actualSum = DigestUtils.sha256Hex(it.bytes)
//            if(sha256 != null && actualSum != sha256) {
//                throw new ChecksumFailedException("", url.toString(), it, sha256, actualSum)
//            }
//        })
//        ArtifactRootVerification folderHasFile = { File installContents ->
//            return installContents.listFiles().find {
//                it.name == fullPath.name
//            }
//        }
//        def artifactDownloader = new ArtifactDownloader(
//                url.toURI(),
//                fullPath.parentFile,
//                project.projectDir,
//                "",
//                folderHasFile,
//                null,
//                sha256 == null? null : verifier
//        )
//        File downloadedFile = artifactDownloader.getFromCache(url.toString(), project.gradle.startParameter.offline, downloaderFactory(fullPath.name))
//
//        project.copy { CopySpec it ->
//            it.from(downloadedFile)
//            it.to(fullPath)
//        }
        return fullPath
    }
}
