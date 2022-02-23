package wooga.gradle.snyk.tasks


import wooga.gradle.snyk.cli.options.ProjectOption
import wooga.gradle.snyk.cli.options.TestOption

import javax.inject.Inject

class Report extends Test {

    @Inject
    Report() {
        reports.json.enabled = true
    }

    @Override
    protected Boolean getIgnoreExitValue() {
        true
    }

    @Override
    void addMainOptions(List<String> args) {
        args.add("test")
        args.addAll(getMappedOptions(this, TestOption))
        args.addAll(getMappedOptions(this, ProjectOption))
    }
}
