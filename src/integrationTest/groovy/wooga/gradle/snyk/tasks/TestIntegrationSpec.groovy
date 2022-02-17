package wooga.gradle.snyk.tasks

class TestIntegrationSpec extends SnykTestBaseIntegrationSpec<Test> {
    @Override
    String getCommandName() {
        return "test"
    }
}
