package wooga.gradle.snyk.tasks

import org.gradle.api.tasks.Nested
import org.gradle.internal.reflect.Instantiator
import wooga.gradle.snyk.cli.commands.TestProjectCommandSpec
import wooga.gradle.snyk.cli.options.CommonOption
import wooga.gradle.snyk.cli.options.ProjectOption
import wooga.gradle.snyk.cli.options.TestOption
import wooga.gradle.snyk.report.SnykReports
import wooga.gradle.snyk.report.SnykReportsImpl

import javax.inject.Inject

abstract class SnykCheckBase extends SnykTask implements TestProjectCommandSpec {
    @Inject
    protected Instantiator getInstantiator() {
        throw new UnsupportedOperationException()
    }

    private SnykReports reports

    @Nested
    SnykReports getReports() {
        reports
    }

    @Inject
    SnykCheckBase() {
        reports = instantiator.newInstance(SnykReportsImpl.class, this)

        reports.sarif.outputLocation.convention(project.layout.buildDirectory.file(new File(this.temporaryDir, "report.sarif").absolutePath))
        reports.json.outputLocation.convention(project.layout.buildDirectory.file(new File(this.temporaryDir, "report.json").absolutePath))

        sarifOutputPath.convention(providers.provider({
            if (reports.sarif.enabled) {
                return reports.sarif.outputLocation.get()
            }
            null
        }))

        jsonOutputPath.convention(providers.provider({
            if (reports.json.enabled) {
                return reports.json.outputLocation.get()
            }
            null
        }))
    }


    @Override
    void addMainOptions(List<String> args) {
        args.add("test")
        args.addAll(getMappedOptions(this, CommonOption))
        args.addAll(getMappedOptions(this, TestOption))
        args.addAll(getMappedOptions(this, ProjectOption))
        args.addAll(getMappedOptions(this, CommonOption))
    }
}
