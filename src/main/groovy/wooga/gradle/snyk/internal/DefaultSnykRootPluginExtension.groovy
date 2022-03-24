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

package wooga.gradle.snyk.internal

import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import wooga.gradle.snyk.ProjectRegistrationHandler
import wooga.gradle.snyk.SnykInstallExtension
import wooga.gradle.snyk.SnykRootPluginExtension
import wooga.gradle.snyk.SnykToHtmlPluginExtension
import wooga.gradle.snyk.tasks.Monitor
import wooga.gradle.snyk.tasks.Report
import wooga.gradle.snyk.tasks.SnykInstall
import wooga.gradle.snyk.tasks.Test

class DefaultSnykRootPluginExtension implements SnykRootPluginExtension, SnykInstallExtension {

    final TaskProvider<SnykInstall> snykInstall
    final TaskProvider<Test> snykTest
    final TaskProvider<Monitor> snykMonitor
    final TaskProvider<Report> snykReport
    final ProjectRegistrationHandler registerProjectHandler
    final Project project
    final SnykToHtmlPluginExtension snykToHtml

    DefaultSnykRootPluginExtension(
            Project project,
            TaskProvider<SnykInstall> snykInstall,
            TaskProvider<Test> snykTest,
            TaskProvider<Monitor> snykMonitor,
            TaskProvider<Report> snykReport,
            SnykToHtmlPluginExtension snykToHtml,
            ProjectRegistrationHandler registerProjectHandler) {
        this.project = project
        this.snykInstall = snykInstall
        this.snykTest = snykTest
        this.snykMonitor = snykMonitor
        this.snykReport = snykReport
        this.snykToHtml = snykToHtml
        this.registerProjectHandler = registerProjectHandler
    }
}
