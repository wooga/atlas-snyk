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

package wooga.gradle.snyk.report

import org.gradle.api.reporting.Report
import org.gradle.api.reporting.ReportContainer
import org.gradle.api.reporting.SingleFileReport
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile

interface SnykReports extends ReportContainer<Report> {
    @Internal
    SingleFileReport getJson()

    @Internal
    SingleFileReport getSarif()

    @Internal
    SnykHtmlReport getHtml()
}
