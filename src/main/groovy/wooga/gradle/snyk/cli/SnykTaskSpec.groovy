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

import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional

trait SnykTaskSpec extends CommonArgumentSpec {

    private final Property<String> token = objects.property(String)

    @Input
    Property<String> getToken() {
        token
    }

    void setToken(Provider<String> value) {
        token.set(value)
    }

    void setToken(String value) {
        token.set(value)
    }

    private final RegularFileProperty logFile = objects.fileProperty()

    @Internal
    RegularFileProperty getLogFile() {
        logFile
    }

    void setLogFile(Provider<RegularFile> value) {
        logFile.set(value)
    }

    void setLogFile(File value) {
        logFile.set(value)
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

    private final Property<String> workingDirectory = objects.property(String)

    /**
     * To override the working directory when invoking the tool
     * @return
     */
    @Input
    @Optional
    Property<String> getWorkingDirectory() {
        workingDirectory
    }

    void setWorkingDirectory(Provider<String> value) {
        workingDirectory.set(value)
    }

}
