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


import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import wooga.gradle.snyk.cli.CommonArgumentSpec
import wooga.gradle.snyk.cli.SnykTaskSpec
import wooga.gradle.snyk.cli.commands.MonitorProjectCommandSpec

trait SnykPluginExtension implements MonitorProjectCommandSpec, SnykTaskSpec, CommonArgumentSpec, SnykInstallSpec {
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

    private final Property<Boolean> autoUpdate = objects.property(Boolean)

    /**
     * @return Whether to auto update the snyk executable if present
     */
    Property<Boolean> getAutoUpdate() {
        autoUpdate
    }

    void setAutoUpdate(Provider<Boolean> value) {
        autoUpdate.set(value)
    }

    void setAutoUpdate(Boolean value) {
        autoUpdate.set(value)
    }
}
