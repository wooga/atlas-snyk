package wooga.gradle.snyk.report

import org.gradle.api.reporting.Report
import org.gradle.api.reporting.ReportContainer
import org.gradle.api.reporting.SingleFileReport
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile

interface SnykReports extends ReportContainer<Report> {
    @Internal
    SingleFileReport getJson()

    @Internal
    SingleFileReport getSarif()
}
