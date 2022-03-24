/*
 * Copyright 2022 Wooga GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wooga.spock.extensions.snyk.interceptor

import com.wooga.gradle.PlatformUtils
import com.wooga.spock.extensions.snyk.SnykSpockAnnotation
import org.spockframework.runtime.extension.AbstractMethodInterceptor
import org.spockframework.runtime.extension.IMethodInvocation
import org.spockframework.runtime.model.NodeInfo

abstract class SnykInterceptor <T extends NodeInfo> extends AbstractMethodInterceptor {
    protected final SnykSpockAnnotation metadata
    protected T info

    SnykInterceptor(SnykSpockAnnotation metadata ) {
        super
        this.metadata = metadata
    }

    private String getExecutableName() {
        def name = metadata.executableName
        if(PlatformUtils.isWindows())
            name += ".exe"
        name
    }

    File downloadSnyk(IMethodInvocation invocation) {
        File snykPath = File.createTempDir(metadata.version, metadata.executableName)
        File snykLocation = new File(snykPath, getExecutableName())

        snykPath.mkdirs()

        def url =  new URL(metadata.snykDownloadURL)
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
