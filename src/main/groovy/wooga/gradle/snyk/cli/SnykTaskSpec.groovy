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

    private final Property<String> executable = objects.property(String)

    @Input
    Property<String> getExecutable() {
        executable
    }

    void setExecutable(Provider<String> value) {
        executable.set(value)
    }

    void setExecutable(String value) {
        executable.set(value)
    }

    private final Property<String> snykVersion = objects.property(String)

    @Input
    Property<String> getSnykVersion() {
        snykVersion
    }

    void setSnykVersion(Provider<String> value) {
        snykVersion.set(value)
    }

    void setSnykVersion(String value) {
        snykVersion.set(value)
    }

    private final DirectoryProperty snykPath = objects.directoryProperty()

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

}
