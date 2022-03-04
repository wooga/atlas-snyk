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

package wooga.gradle.snyk.tasks

import wooga.gradle.snyk.cli.options.CommonOption
import wooga.gradle.snyk.cli.options.MonitorOption
import wooga.gradle.snyk.cli.commands.MonitorProjectCommandSpec
import wooga.gradle.snyk.cli.options.ProjectOption
import wooga.gradle.snyk.cli.options.TestOption

/**
 * The snyk monitor command creates a project in your Synk account to be continuously
 * monitored for open source vulnerabilities and license issues.
 * After running this command, log in to the Snyk website and view your projects to see the monitor.
 * (https://docs.snyk.io/features/snyk-cli/commands/monitor)
 */
class Monitor extends SnykTask implements MonitorProjectCommandSpec {

    @Override
    void addMainOptions(List<String> args) {
        args.add("monitor")
        args.addAll(getMappedOptions(this, CommonOption))
        args.addAll(getMappedOptions(this, MonitorOption))
        args.addAll(getMappedOptions(this, TestOption))
        args.addAll(getMappedOptions(this, ProjectOption))
    }
}
