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
import wooga.gradle.snyk.cli.commands.TestProjectCommandSpec
import wooga.gradle.snyk.cli.options.CommonOption
import wooga.gradle.snyk.cli.options.ProjectOption
import wooga.gradle.snyk.cli.options.TestOption
import wooga.gradle.snyk.report.SnykReports
import wooga.gradle.snyk.report.SnykReportsImpl

import javax.inject.Inject

/**
 * The snyk test command checks projects for open source vulnerabilities and license issues.
 * The test command tries to auto-detect supported manifest files with dependencies and test those.
 * (https://docs.snyk.io/features/snyk-cli/commands/test)
 */
class Test extends SnykTask implements TestProjectCommandSpec {

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
    Test() {
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
    }
}
