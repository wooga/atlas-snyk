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

package wooga.gradle.snyk.report.internal

import com.wooga.gradle.io.FileUtils
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.reporting.internal.TaskGeneratedSingleFileReport
import org.gradle.process.ExecResult
import org.gradle.process.ExecSpec
import wooga.gradle.OptionAggregator
import wooga.gradle.snyk.cli.options.SnykToHtmlOption
import wooga.gradle.snyk.report.SnykHtmlReport

import javax.inject.Inject

abstract class SnykHtmlReportImpl extends TaskGeneratedSingleFileReport implements SnykHtmlReport, OptionAggregator {

    private final Project project

    @Inject
    SnykHtmlReportImpl(String name, Task task) {
        super(name, task)
        project = task.project
        executableName.convention("snyk-to-html")
    }

    ExecResult generate() {
        if(this.required.present && this.required.get() && input.present && input.get().asFile.exists()) {
            def arguments = getMappedOptions(this, SnykToHtmlOption)

            def _executable
            if (this.snykPath.present) {
                _executable = this.snykPath.file(this.executableName).get().asFile.path
            } else {
                _executable = this.executableName.get()
            }

            if (logFile.present){
                FileUtils.ensureFile(logFile)
            }

            return project.exec(new Action<ExecSpec>() {
                @Override
                void execute(ExecSpec exec) {

                    exec.with {
                        executable _executable
                        args = arguments

                        standardOutput = getOutputStream(logFile.asFile.getOrNull())
                        errorOutput = getOutputStream(logFile.asFile.getOrNull())
                    }
                }
            })
        }
        return null
    }
}
