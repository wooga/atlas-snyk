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

package com.wooga.spock.extensions.snyk

import com.wooga.gradle.PlatformUtils
import com.wooga.spock.extensions.snyk.interceptor.SnykFieldInterceptor
import com.wooga.spock.extensions.snyk.interceptor.SnykInterceptor
import com.wooga.spock.extensions.snyk.interceptor.SnykSharedFieldInterceptor
import org.spockframework.runtime.extension.AbstractAnnotationDrivenExtension
import org.spockframework.runtime.model.FieldInfo

class SnykToHtmlExtension extends AbstractAnnotationDrivenExtension<SnykToHtml> {

    @Override
    void visitFieldAnnotation(SnykToHtml annotation, FieldInfo field) {
        SnykInterceptor interceptor
        def wrapper = new SnykSpockAnnotation() {
            @Override
            String getExecutableName() {
                annotation.executableName()
            }

            @Override
            String getVersion() {
                annotation.version()
            }

            @Override
            String getSnykDownloadURL() {
                if(PlatformUtils.isWindows()) {
                    return "https://static.snyk.io/snyk-to-html/${version}/snyk-to-html-win.exe"
                }

                if(PlatformUtils.isMac()) {
                    return "https://static.snyk.io/snyk-to-html/${version}/snyk-to-html-macos"
                }

                if(PlatformUtils.isLinux()) {
                    return "https://static.snyk.io/snyk-to-html/${version}/snyk-to-html-linux"
                }
            }
        }

        if (field.isShared()) {
            interceptor = new SnykSharedFieldInterceptor(wrapper)
        } else {
            interceptor = new SnykFieldInterceptor(wrapper)
        }

        interceptor.install(field)
    }
}

