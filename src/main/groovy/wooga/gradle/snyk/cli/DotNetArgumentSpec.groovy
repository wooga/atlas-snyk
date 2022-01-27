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
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.options.Option

trait DotNetArgumentSpec extends BaseSpec {
    private final Property<Boolean> assetsProjectName = objects.property(Boolean)

    @Input
    @Optional
    @Option(option = "assets-project-name", description = """
    When monitoring a .NET project using NuGet PackageReference use the project name in
    project.assets.json, if found.
    """)
    Property<Boolean> getAssetsProjectName() {
        assetsProjectName
    }

    void setAssetsProjectName(Provider<Boolean> value) {
        assetsProjectName.set(value)
    }

    void setAssetsProjectName(Boolean value) {
        assetsProjectName.set(value)
    }

    private final DirectoryProperty packagesFolder = objects.directoryProperty()

    @InputDirectory
    @Optional
    DirectoryProperty getPackagesFolder() {
        packagesFolder
    }

    void setPackagesFolder(Provider<Directory> value) {
        packagesFolder.set(value)
    }

    void setPackagesFolder(File value) {
        packagesFolder.set(value)
    }

    @Option(option = "packages-folder", description = "Specify a custom path to the packages folder.")
    packagesFolder(String dir) {
        packagesFolder.set(layout.projectDirectory.dir(dir))
    }

    private final Property<String> projectNamePrefix = objects.property(String)

    @Input
    @Optional
    @Option(option = "project-name-prefix", description = """
    When monitoring a .NET project, use this option to add a custom prefix to the name of files
    inside a project along with any desired separators, for example, snyk monitor
    --file=my-project.sln --project-name-prefix=my-group/. This is useful when you have multiple
    projects with the same name in other .sln files.
    """)
    Property<String> getProjectNamePrefix() {
        projectNamePrefix
    }

    void setProjectNamePrefix(Provider<String> value) {
        projectNamePrefix.set(value)
    }

    void setProjectNamePrefix(String value) {
        projectNamePrefix.set(value)
    }
}
