package wooga.gradle.snyk.tasks

class ReportIntegrationSpec extends SnykTestBaseIntegrationSpec<Report> {
    @Override
    String getCommandName() {
        "test"
    }

    def setup() {
        appendToSubjectTask("""
        reports.json.enabled = false
        """.stripIndent())
    }
}
