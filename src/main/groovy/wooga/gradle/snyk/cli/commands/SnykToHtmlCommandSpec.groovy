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

package wooga.gradle.snyk.cli.commands

import com.wooga.gradle.io.LogFileSpec
import com.wooga.gradle.io.OutputStreamSpec
import wooga.gradle.OptionMapper
import wooga.gradle.snyk.cli.SnykToHtmlSpec
import wooga.gradle.snyk.cli.options.SnykToHtmlOption

trait SnykToHtmlCommandSpec implements SnykToHtmlSpec, OutputStreamSpec, LogFileSpec, OptionMapper<SnykToHtmlOption> {

    @Override
    String getOption(SnykToHtmlOption option) {
        def value = null

        switch (option) {
            case SnykToHtmlOption.input:
                value = input.get().asFile
                break
            case SnykToHtmlOption.output:
                value = output.get().asFile
                break
            case SnykToHtmlOption.summary:
                if (summaryOnly.present && summaryOnly.get()) {
                    value = true
                }
                break
            case SnykToHtmlOption.actionableRemediation:
                if (actionableRemediation.present && actionableRemediation.get()) {
                    value = true
                }
        }

        if (value != null) {
            def output = option.compose(value)
            return output
        }
        null
    }
}
