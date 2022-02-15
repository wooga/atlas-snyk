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

import wooga.gradle.snyk.cli.commands.TestProjectCommandSpec
import wooga.gradle.snyk.cli.options.ProjectOption
import wooga.gradle.snyk.cli.options.TestOption

/**
 * The snyk test command checks projects for open source vulnerabilities and license issues.
 * The test command tries to auto-detect supported manifest files with dependencies and test those.
 * (https://docs.snyk.io/features/snyk-cli/commands/test)
 */
class Test extends SnykTask implements TestProjectCommandSpec {

  @Override
  void addMainOptions(List<String> args) {
    args.add("test")
    args.addAll(getMappedOptions(this, TestOption))
    args.addAll(getMappedOptions(this, ProjectOption))
  }

  static String composeStartMessage(String workingDir) {
    "Testing ${workingDir.replace("\\\\", '\\')}"
  }

  static final String debugStartMessage = "===== DEBUG INFORMATION START ====="
}
