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

enum SnykExitCodeResult {

  Success("no vulnerabilities found"),
  VulnerabilitiesFound("Vulnerabilities found"),
  RerunCommand("Try to re-run command"),
  Failure_NoSupportedProjects("No supported projects detected")

  private final String message

  String getMessage() {
    message
  }

  SnykExitCodeResult(String message) {
    this.message = message
  }
}

class SnykExecutionException extends Exception {
  private int exitCode

  int getExitCode() {
    exitCode
  }

  SnykExecutionException(int exitCode, String message) {
    super(message)
    this.exitCode = exitCode
  }

  SnykExecutionException(int exitCode)  {
    super(SnykExitCodeResult.values()[exitCode].message)
    this.exitCode = exitCode
  }
}
