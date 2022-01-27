package wooga.gradle.snyk.tasks.internal

interface Downlader {

    File download(File fullPath, URL url)
    File download(File fullPath, URL url, String sha256)

}