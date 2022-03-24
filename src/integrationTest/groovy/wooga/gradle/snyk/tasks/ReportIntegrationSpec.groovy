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

import wooga.gradle.snyk.SnykPlugin

class ReportIntegrationSpec extends SnykTestBaseIntegrationSpec<Report> {
    @Override
    String getCommandName() {
        "test"
    }

    def setup() {
        appendToSubjectTask("""
        reports.html.required = false
        """.stripIndent())
    }

    def "generates html report by default"() {
        given: "a snyk setup"
        and: "snyk plugin applied with conventions"
        buildFile << """
            ${applyPlugin(SnykPlugin)}
        """.stripIndent()

        setSnykToken()
        setDownloadedSnyk()
        setDownloadedSnykToHtml()

        and: "bring back the actual default value"
        appendToSubjectTask("""
        reports.html.required.set(true)
        """)

        and: "an empty policy file (see: https://github.com/snyk/policy/issues/61)"
        setSnykPolicy()

        and: "a mocked project"
        copyToProject(getResourceFile("net_project"), projectDir)

        and: "project-specific configurations for it"
        appendToSubjectTask("""
            packageFile=${wrapValueBasedOnType("net_project.sln", File)} 
            """.stripIndent())

        and: "a future report file"
        def htmlReport = new File(projectDir, htmlReportLocation)
        assert !htmlReport.exists()

        appendToSubjectTask("""
            reports.html.outputLocation=${wrapValueBasedOnType(htmlReportLocation, File)}  
        """.stripIndent())

        when:
        def result = runTasks(subjectUnderTestName)

        then:
        result.success
        result.wasExecuted(subjectUnderTestName)
        !result.wasSkipped(subjectUnderTestName)
        htmlReport.exists()

        where:
        htmlReportLocation = "build/reports/report.html"
    }
}
