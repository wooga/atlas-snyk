package wooga.gradle.snyk.tasks

import spock.lang.Unroll

class TestIntegrationSpec extends SnykTestBaseIntegrationSpec<Test> {
    @Override
    String getCommandName() {
        return "test"
    }


}
