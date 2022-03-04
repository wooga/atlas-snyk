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
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.options.Option
import wooga.gradle.OptionMapper
import wooga.gradle.snyk.cli.options.CommonOption

trait CommonArgumentSpec extends BaseSpec implements OptionMapper<CommonOption> {

    @Internal
    ProviderFactory getProviderFactory() {
        getProviders()
    }

    @Override
    String getOption(CommonOption option) {
        def value = null

        switch (option) {
            case CommonOption.debug:
                if (debug.present && debug.get()) {
                    value = true
                }
                break
        }

        if (value != null) {
            def output = option.compose(value)
            return output
        }
        null
    }

    private final Property<Boolean> insecure = objects.property(Boolean)

    @Input
    @Optional
    @Option(option = "insecure", description = """
    Ignore unknown certificate authorities
    """)
    Property<Boolean> getInsecure() {
        insecure
    }

    void setInsecure(Provider<Boolean> value) {
        insecure.set(value)
    }

    void setInsecure(Boolean value) {
        insecure.set(value)
    }

    private final Property<Boolean> debug = objects.property(Boolean)

    @Input
    @Optional
    Property<Boolean> getDebug() {
        debug
    }

    void setDebug(Provider<Boolean> value) {
        debug.set(value)
    }

    void setDebug(Boolean value) {
        debug.set(value)
    }
}
