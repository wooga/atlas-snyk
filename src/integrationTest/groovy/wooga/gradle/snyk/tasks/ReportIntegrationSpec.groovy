package wooga.gradle.snyk.tasks

import wooga.gradle.snyk.SnykPlugin

class ReportIntegrationSpec extends SnykTestBaseIntegrationSpec<Report> {
    @Override
    String getCommandName() {
        "test"
    }

    def setup() {
        appendToSubjectTask("""
        reports.json.required = false
        """.stripIndent())
    }

    def "generates json report by default"() {
        given: "a snyk setup"
        and: "snyk plugin applied with conventions"
        buildFile << """
            ${applyPlugin(SnykPlugin)}
        """.stripIndent()

        setSnykToken()
        setDownloadedSnyk()

        and: "bring back the actual default value"
        appendToSubjectTask("""
        reports.json.required.set(null)
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
        def jsonReport = new File(projectDir, jsonReportLocation)
        assert !jsonReport.exists()

        appendToSubjectTask("""
            reports.json.outputLocation=${wrapValueBasedOnType(jsonReportLocation, File)}  
        """.stripIndent())

        when:
        def result = runTasks(subjectUnderTestName)

        then:
        result.success
        result.wasExecuted(subjectUnderTestName)
        !result.wasSkipped(subjectUnderTestName)
        jsonReport.exists()

        where:
        jsonReportLocation = "build/reports/report.json"
    }
}
