package wooga.gradle.snyk.report

import org.gradle.api.Task
import org.gradle.api.internal.CollectionCallbackActionDecorator
import org.gradle.api.reporting.ConfigurableReport
import org.gradle.api.reporting.Report
import org.gradle.api.reporting.SingleFileReport
import org.gradle.api.reporting.internal.TaskGeneratedSingleFileReport
import org.gradle.api.reporting.internal.TaskReportContainer

class SnykReportsImpl extends TaskReportContainer<Report> implements SnykReports {

    SnykReportsImpl(Task task) {
        super(ConfigurableReport.class, task, CollectionCallbackActionDecorator.NOOP)
        add(TaskGeneratedSingleFileReport.class, "json", task)
        add(TaskGeneratedSingleFileReport.class, "sarif", task)
    }

    @Override
    SingleFileReport getJson() {
        (TaskGeneratedSingleFileReport) getByName("json")
    }

    @Override
    SingleFileReport getSarif() {
        (TaskGeneratedSingleFileReport) getByName("sarif")
    }
}
