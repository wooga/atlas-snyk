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
import wooga.gradle.snyk.cli.SnykMonitorArgumentsSpec
import wooga.gradle.snyk.cli.SnykTaskSpec

trait SnykPluginExtension extends SnykMonitorArgumentsSpec, SnykTaskSpec, CommonArgumentSpec {
    private final Property<Boolean> autoDownloadSnykCli = objects.property(Boolean)

    Property<Boolean> getAutoDownloadSnykCli() {
        autoDownloadSnykCli
    }

    void setAutoDownloadSnykCli(Provider<Boolean> value) {
        autoDownloadSnykCli.set(value)
    }

    void setAutoDownloadSnykCli(Boolean value) {
        autoDownloadSnykCli.set(value)
    }

    private final Property<Boolean> autoUpdateSnykCli = objects.property(Boolean)

    Property<Boolean> getAutoUpdateSnykCli() {
        autoUpdateSnykCli
    }

    void setAutoUpdateSnykCli(Provider<Boolean> value) {
        autoUpdateSnykCli.set(value)
    }

    void setAutoUpdateSnykCli(Boolean value) {
        autoUpdateSnykCli.set(value)
    }
}
