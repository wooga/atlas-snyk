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

package wooga.gradle.snyk.cli.commands


import org.gradle.api.internal.file.FileOperations
import wooga.gradle.OptionMapper
import wooga.gradle.snyk.cli.SnykTestOutputSpec
import wooga.gradle.snyk.cli.SnykTestSpec
import wooga.gradle.snyk.cli.options.TestOption

import javax.inject.Inject

/**
 * Provides properties for the Test command
 */
trait TestProjectCommandSpec implements SnykTestSpec, SnykTestOutputSpec, OptionMapper<TestOption> {

    @Inject
    FileOperations getFileOperations() {
        throw new UnsupportedOperationException();
    }

    @Override
    String getOption(TestOption option) {
        def value = null

        switch (option) {
            case TestOption.allProjects:
                if (allProjects.present && allProjects.get()) {
                    value = true
                }
                break

            case TestOption.projectName:
                if (projectName.present) {
                    value = projectName.get()
                }
                break

            case TestOption.detectionDepth:
                if (detectionDepth.present) {
                    value = detectionDepth.get()
                }
                break

            case TestOption.exclude:
                if (exclude.present && !exclude.get().isEmpty()) {
                    value = exclude.get()
                }
                break

            case TestOption.pruneRepeatedSubDependencies:
                if (pruneRepeatedSubDependencies.present && pruneRepeatedSubDependencies.get()) {
                    value = true
                }
                break

            case TestOption.printDependencies:
                if (printDependencies.present && printDependencies.get()) {
                    value = true
                }
                break

            case TestOption.remoteRepoUrl:
                if (remoteRepoUrl.present) {
                    value = remoteRepoUrl.get()
                }
                break

            case TestOption.includeDevelopmentDependencies:
                if (includeDevelopmentDependencies.present && includeDevelopmentDependencies.get()) {
                    value = true
                }
                break

            case TestOption.orgName:
                if (orgName.present) {
                    value = orgName.get()
                }
                break

            case TestOption.packageFile:
                if (packageFile.present) {
                    value = new File(fileOperations.relativePath(packageFile.asFile.get()))
                }
                break

            case TestOption.packageManager:
                if (packageManager.present) {
                    value = packageManager.get()
                }
                break

            case TestOption.ignorePolicy:
                if (ignorePolicy.present && ignorePolicy.get()) {
                    value = true
                }
                break

            case TestOption.showVulnerablePaths:
                if (showVulnerablePaths.present) {
                    value = showVulnerablePaths.get()
                }
                break

            case TestOption.targetReference:
                if (targetReference.present) {
                    value = targetReference.get()
                }
                break

            case TestOption.policyPath:
                if (policyPath.present) {
                    value = policyPath.asFile.get()
                }
                break

            case TestOption.printJson:
                if (printJson.present && printJson.get()) {
                    value = true
                }
                break

            case TestOption.jsonOutputPath:
                if (jsonOutputPath.present) {
                    value = jsonOutputPath.getAsFile().get()
                }
                break

            case TestOption.printSarif:
                if (printSarif.present && printSarif.get()) {
                    value = true
                }
                break

            case TestOption.sarifOutputPath:
                if (sarifOutputPath.present) {
                    value = sarifOutputPath.getAsFile().get()
                }
                break

            case TestOption.severityThreshold:
                if (severityThreshold.present) {
                    value = severityThreshold.get()
                }
                break

            case TestOption.failOn:
                if (failOn.present) {
                    value = failOn.get()
                }
                break
        }

        if (value != null) {
            def output = option.compose(value)
            return output
        }
        null
    }
}
