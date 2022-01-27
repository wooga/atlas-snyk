package wooga.gradle.snyk.tasks.internal

import org.apache.commons.codec.digest.DigestUtils
import org.gradle.api.Project
import org.gradle.wrapper.IDownload
import org.ysb33r.grolifant.internal.v4.downloader.Downloader

class CachedGroliphantDownloader implements Downlader {

    private final Closure<IDownload> downloaderFactory

    CachedGroliphantDownloader(Project project) {
        this.downloaderFactory = {
            String distName -> Downloader.create(distName, project.gradle.startParameter.logLevel).downloader
        }
    }

    @Override
    File download(File dest, URL source, String sha256 = null) {
        if (!dest.file) {
            forceDownload(dest, source, sha256)
        } else {
            if(!checksum(dest, sha256)) {
                forceDownload(dest, source, sha256)
            }
        }
        return dest
    }

    void forceDownload(File dest, URL source, String sha256) {
        IDownload downloader = this.downloaderFactory(dest.name)
        if(dest.exists()) {
            dest.delete()
        }
        dest.createNewFile()
        downloader.download(source.toURI(), dest)
        if(!checksum(dest, sha256)) {
            throw new Exception("Error while matching checksums\nexpected: ${sha256} \nactual: ${DigestUtils.sha256Hex(dest.bytes)}")
        }
    }

    private static boolean checksum(File file, String sha256) {
        def actualSum = DigestUtils.sha256Hex(file.bytes)
        return sha256 == null || actualSum == sha256
    }
}
