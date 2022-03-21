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
        reports.json.required.convention(true)
    }

    @Override
    protected Boolean getIgnoreExitValue() {
        true
    }
}
