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

package wooga.gradle.snyk.cli.options

import wooga.gradle.OptionBuilder
import wooga.gradle.OptionSpec
import wooga.gradle.snyk.cli.SnykBooleanOption
import wooga.gradle.snyk.cli.SnykFileOption
import wooga.gradle.snyk.cli.SnykStringOption

enum SnykToHtmlOption implements OptionSpec {

    input("--input", new SnykFileOption()),
    output("--output", new SnykFileOption()),
    summary("--summary", new SnykBooleanOption()),
    actionableRemediation("--actionable-remediation", new SnykStringOption())

    private String flag
    private OptionBuilder builder

    String getFlag() {
        flag
    }

    OptionBuilder getBuilder() {
        builder
    }

    SnykToHtmlOption(String flag, OptionBuilder builder) {
        this.flag = flag
        this.builder = builder
    }
}
