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

trait GoArgumentSpec extends BaseSpec {
    private final Property<SnykCLIOptions.FailOnOption> failOn = objects.property(SnykCLIOptions.FailOnOption)

    @Input
    @Optional
    @Option(option = "fail-on", description = """
    Fail only when there are vulnerabilities that can be fixed.
    -  all: fail when there is at least one vulnerability that can be either upgraded or patched.
    -  upgradable: fail when there is at least one vulnerability that can be upgraded.
    -  patchable: fail when there is at least one vulnerability that can be patched.
    If vulnerabilities do not have a fix and this option is being used, tests pass.
    """)
    Property<SnykCLIOptions.FailOnOption> getFailOn() {
        failOn
    }

    void setFailOn(Provider<SnykCLIOptions.FailOnOption> value) {
        failOn.set(value)
    }

    void setFailOn(SnykCLIOptions.FailOnOption value) {
        failOn.set(value)
    }


}
