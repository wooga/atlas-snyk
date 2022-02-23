package wooga.gradle.snyk.tasks


class Report extends Test {

    Report() {
        reports.json.enabled = true
    }

    @Override
    protected Boolean getIgnoreExitValue() {
        true
    }
}
