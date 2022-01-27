package wooga.gradle.snyk.tasks.internal

class SnykDownloadAsset {
    final String name
    final String version
    final URL url
    final String sha256

    SnykDownloadAsset(String name, String version, URL url, String sha256) {
        this.name = name
        this.version = version
        this.url = url
        this.sha256 = sha256.trim().split(" ")[0]
    }

    public File download(File installationDir, Downlader downloader) {
        installationDir.mkdirs()
        File fullPath = new File(installationDir, name)
        File snykExecutable = downloader.download(fullPath, url, sha256)
        snykExecutable.setExecutable(true, false)
        return snykExecutable
    }
}
