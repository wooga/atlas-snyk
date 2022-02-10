package wooga.gradle.snyk

import spock.lang.Specification
import spock.lang.Unroll
import wooga.gradle.snyk.cli.SnykExecutionException
import wooga.gradle.snyk.cli.SnykExitCodeResult
import wooga.gradle.snyk.tasks.SnykTask

class SnykExecutionExceptionTest extends Specification {

    def "handles success exit code correctly"() {
        when:
        SnykTask.handleExitCode(exitCode)

        then:
        noExceptionThrown()

        where:
        exitCode = 0
    }

    @Unroll
    def "handles failure exit code correctly"() {

        when:
        SnykTask.handleExitCode(exitCode)

        then:
        def exception = thrown(SnykExecutionException)
        exception.exitCode == exitCode
        exception.message == message

        where:
        exitCode | message
        1        | SnykExitCodeResult.VulnerabilitiesFound.message
        2        | SnykExitCodeResult.RerunCommand.message
        3        | SnykExitCodeResult.Failure_NoSupportedProjects.message
    }


}
