package com.wooga.spock.extensions.snyk.interceptor

import com.wooga.gradle.PlatformUtils
import com.wooga.spock.extensions.snyk.Snyk
import org.spockframework.runtime.extension.AbstractMethodInterceptor
import org.spockframework.runtime.extension.IMethodInvocation
import org.spockframework.runtime.model.NodeInfo

abstract class SnykInterceptor <T extends NodeInfo> extends AbstractMethodInterceptor {
    protected final Snyk metadata
    protected T info

    SnykInterceptor(Snyk metadata) {
        super
        this.metadata = metadata
    }

    private static String getSnykDownloadURL(String version = "latest") {
        if(PlatformUtils.isWindows()) {
            return "https://static.snyk.io/cli/${version}/snyk-win.exe"
        }

        if(PlatformUtils.isMac()) {
            return "https://static.snyk.io/cli/${version}/snyk-macos"
        }

        if(PlatformUtils.isLinux()) {
            return "https://static.snyk.io/cli/${version}/snyk-linux"
        }
    }

    private String getExecutableName() {
        def name = metadata.executableName()
        if(PlatformUtils.isWindows())
            name += ".exe"
        name
    }

    File downloadSnyk(IMethodInvocation invocation) {
        File snykPath = File.createTempDir(metadata.version(), "snyk")
        File snykLocation = new File(snykPath, getExecutableName())

        snykPath.mkdirs()

        def url =  new URL(getSnykDownloadURL(metadata.version()))
        url.withInputStream { i ->
            snykLocation.withOutputStream {
                it << i
            }
        }

        if (!snykLocation.exists()) {
            throw new Exception("Failed to download snyk")
        }
        snykLocation.setExecutable(true, true)
        snykLocation
    }

    abstract void install(T info)
}
