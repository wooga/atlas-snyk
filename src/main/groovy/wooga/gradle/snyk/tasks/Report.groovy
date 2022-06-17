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
/**
 * The {@code Report} task has the same functionality as {@link Test} task type.
 * The main difference is that the {@code Report} task never fails and will
 * create json reports by default.
 *
 * @see wooga.gradle.snyk.tasks.Test
 */
class Report extends SnykCheckBase {

    Report() {
        reports.html.required.set(true)
        ignoreExitValue.set(true)
    }
}
