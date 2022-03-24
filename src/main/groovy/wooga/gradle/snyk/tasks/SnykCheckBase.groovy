/*
 * Copyright 2022 Wooga GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package wooga.gradle.snyk.tasks

import org.gradle.api.tasks.Nested
import org.gradle.internal.reflect.Instantiator
import org.gradle.process.ExecResult
import wooga.gradle.snyk.cli.SnykExecutionException
import wooga.gradle.snyk.cli.commands.ProjectCommandSpec
import wooga.gradle.snyk.cli.commands.TestProjectCommandSpec
import wooga.gradle.snyk.cli.options.CommonOption
import wooga.gradle.snyk.cli.options.ProjectOption
import wooga.gradle.snyk.cli.options.TestOption
import wooga.gradle.snyk.report.SnykReports
import wooga.gradle.snyk.report.internal.SnykReportsImpl

import javax.inject.Inject

abstract class SnykCheckBase extends SnykTask implements TestProjectCommandSpec, ProjectCommandSpec {
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
        reports.html.outputLocation.convention(project.layout.buildDirectory.file(new File(this.temporaryDir, "report.html").absolutePath))

        sarifOutputPath.convention(reports.sarif.required.flatMap({
            if (it) {
                return reports.sarif.outputLocation
            }
            null
        }))

        jsonOutputPath.convention(reports.json.required.flatMap({
            if (it) {
                return reports.json.outputLocation
            }
            null
        }))
    }

    @Override
    void postAction(ExecResult _) {
        //TODO: move the generation into an action. I wanted to have this in one place but think it is no longer a good idea.
        def r = ((SnykReportsImpl) reports).htmlReportAction.generate()
        if (r && r.exitValue != 0) {
            throw new SnykExecutionException(r.exitValue, "failed to convert json report to html")
        }
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
