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

package wooga.gradle.snyk

import groovy.transform.stc.ClosureParams
import groovy.transform.stc.FromString
import org.gradle.api.Action
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
import org.gradle.util.ConfigureUtil
import wooga.gradle.snyk.cli.SnykToHtmlSpec
import wooga.gradle.snyk.tasks.SnykToHtmlInstall

trait SnykToHtmlPluginExtension extends SnykInstallExtension implements SnykToHtmlSpec {

    private final Property<Boolean> autoDownload = objects.property(Boolean)

    /**
     * @return Whether to auto download the snyk executable if not found
     */
    Property<Boolean> getAutoDownload() {
        autoDownload
    }

    void setAutoDownload(Provider<Boolean> value) {
        autoDownload.set(value)
    }

    void setAutoDownload(Boolean value) {
        autoDownload.set(value)
    }

    abstract TaskProvider<SnykToHtmlInstall> getSnykToHtmlInstall()

    void snykToHtmlInstall(Action<SnykToHtmlInstall> action) {
        snykToHtmlInstall.configure(action)
    }

    void snykToHtmlInstall(@ClosureParams(value = FromString.class, options = ["wooga.gradle.snyk.tasks.SnykToHtmlInstall"]) Closure configure) {
        snykToHtmlInstall.configure(ConfigureUtil.configureUsing(configure))
    }
}
