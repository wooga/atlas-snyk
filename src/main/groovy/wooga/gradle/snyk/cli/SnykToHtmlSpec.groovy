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

package wooga.gradle.snyk.cli

import com.wooga.gradle.BaseSpec
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile

trait SnykToHtmlSpec extends BaseSpec {
    private final Property<Boolean> summaryOnly = objects.property(Boolean)

    /**
     * Generates an HTML with only the summary, instead of the details report
     */
    @Input
    @Optional
    Property<Boolean> getSummaryOnly() {
        summaryOnly
    }

    void setSummaryOnly(Provider<Boolean> value) {
        summaryOnly.set(value)
    }

    void setSummaryOnly(Boolean value) {
        summaryOnly.set(value)
    }

    private final Property<Boolean> actionableRemediation = objects.property(Boolean)

    /**
     * Displays actionable remediation info if available
     * @return
     */
    @Input
    @Optional
    Property<Boolean> getActionableRemediation() {
        actionableRemediation
    }

    void setActionableRemediation(Provider<Boolean> value) {
        actionableRemediation.set(value)
    }

    void setActionableRemediation(Boolean value) {
        actionableRemediation.set(value)
    }

    private final Property<String> executableName = objects.property(String)

    /**
     * The name to the executable for the CLI tool.
     * This is used only if the {@code snykPath} property is not set.
     */
    @Input
    Property<String> getExecutableName() {
        executableName
    }

    void setExecutableName(Provider<String> value) {
        executableName.set(value)
    }

    void setExecutableName(String value) {
        executableName.set(value)
    }

    private final DirectoryProperty snykPath = objects.directoryProperty()

    /**
     * The path to the directory for the CLI tool. If set,
     * This is used over the the {@code executableName} property
     */
    @Internal
    DirectoryProperty getSnykPath() {
        snykPath
    }

    void setSnykPath(Provider<Directory> value) {
        snykPath.set(value)
    }

    void setSnykPath(File value) {
        snykPath.set(value)
    }

    private final RegularFileProperty input = objects.fileProperty()

    @Internal
    RegularFileProperty getInput() {
        input
    }

    void setInput(Provider<RegularFile> value) {
        input.set(value)
    }

    void setInput(File value) {
        input.set(value)
    }

    private final RegularFileProperty output = objects.fileProperty()

    @OutputFile
    RegularFileProperty getOutput() {
        output
    }

    void setOutput(Provider<RegularFile> value) {
        output.set(value)
    }

    void setOutput(File value) {
        output.set(value)
    }
}
