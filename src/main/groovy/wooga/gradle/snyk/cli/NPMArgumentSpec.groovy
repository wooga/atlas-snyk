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

package wooga.gradle.snyk.cli

import com.wooga.gradle.BaseSpec
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.options.Option

trait NPMArgumentSpec extends BaseSpec {
    private final Property<Boolean> strictOutOfSync = objects.property(Boolean)

    @Input
    @Optional
    @Option(option = "strict-out-of-sync", description = """
    Control testing out of sync lockfiles.
    """)
    Property<Boolean> getStrictOutOfSync() {
        strictOutOfSync
    }

    void setStrictOutOfSync(Provider<Boolean> value) {
        strictOutOfSync.set(value)
    }

    void setStrictOutOfSync(Boolean value) {
        strictOutOfSync.set(value)
    }
}
