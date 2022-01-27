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
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.options.Option

trait GradleArgumentsSpec extends BaseSpec {
    private final Property<String> subProject = objects.property(String)

    @Input
    @Optional
    @Option(option = "sub-project", description = """
    For Gradle "multi project" configurations, test a specific sub-project.
    """)
    Property<String> getSubProject() {
        subProject
    }

    void setSubProject(Provider<String> value) {
        subProject.set(value)
    }

    void setSubProject(String value) {
        subProject.set(value)
    }

    private final Property<Boolean> allSubProjects = objects.property(Boolean)

    @Input
    @Optional
    @Option(option = "all-sub-projects", description = """
    For "multi project" configurations, test all sub-projects.
    """)
    Property<Boolean> getAllSubProjects() {
        allSubProjects
    }

    void setAllSubProjects(Provider<Boolean> value) {
        allSubProjects.set(value)
    }

    void setAllSubProjects(Boolean value) {
        allSubProjects.set(value)
    }

    private final Property<String> configurationMatching = objects.property(String)

    @Input
    @Optional
    @Option(option = "configuration-matching", description = """
    Resolve dependencies using only configuration(s) that match the specified Java regular
    expression, for example, ^releaseRuntimeClasspath\$.
    """)
    Property<String> getConfigurationMatching() {
        configurationMatching
    }

    void setConfigurationMatching(Provider<String> value) {
        configurationMatching.set(value)
    }

    void setConfigurationMatching(String value) {
        configurationMatching.set(value)
    }

    private final ListProperty<String> configurationAttributes = objects.listProperty(String)

    @Input
    @Optional
    ListProperty<String> getConfigurationAttributes() {
        configurationAttributes
    }

    void setConfigurationAttributes(Provider<Iterable<String>> attributes) {
        configurationAttributes.set(attributes)
    }

    void setConfigurationAttributes(Iterable<String> attributes) {
        configurationAttributes.set(attributes)
    }

    void configurationAttributes(String attribute) {
        configurationAttributes.add(attribute)
    }

    void configurationAttributes(String... attributes) {
        configurationAttributes.addAll(attributes)
    }

    void configurationAttributes(Iterable<String> attributes) {
        configurationAttributes.addAll(attributes)
    }

    @Option(option = "configuration-attributes", description = """
    Select certain values of configuration attributes to install dependencies and perform dependency
    resolution, for example, buildtype:release,usage:java-runtime.
    """)
    void configurationAttributesOption(String attributes) {
        configurationAttributes(attributes.trim().split(","))
    }

    private final Property<Boolean> reachable = objects.property(Boolean)

    @Input
    @Optional
    @Option(option = "reachable", description = """
    Analyze your source code to find which vulnerable functions and packages are called.
    """)
    Property<Boolean> getReachable() {
        reachable
    }

    void setReachable(Provider<Boolean> value) {
        reachable.set(value)
    }

    void setReachable(Boolean value) {
        reachable.set(value)
    }

    private final Property<Integer> reachableTimeout = objects.property(Integer)

    @Input
    @Optional
    Property<Integer> getReachableTimeout() {
        reachableTimeout
    }

    void setReachableTimeout(Provider<Integer> value) {
        reachableTimeout.set(value)
    }

    void setReachableTimeout(Integer value) {
        reachableTimeout.set(value)
    }

    @Option(option = "reachable-timeout", description = """
    Analyze your source code to find which vulnerable functions and packages are called.
    """)
    void reachableTimeout(String value) {
        reachableTimeout.set(Integer.parseInt(value))
    }

    private final RegularFileProperty initScript = objects.fileProperty()

    @InputFile
    @Optional
    RegularFileProperty getInitScript() {
        initScript
    }

    void setInitScript(Provider<RegularFile> value) {
        initScript.set(value)
    }

    void setInitScript(File value) {
        initScript.set(value)
    }

    @Option(option = "gradle-init-script", description = """
    Use for projects that contain a Gradle initialization script.
    """)
    void initScriptOption(String value) {
        initScript.set(layout.projectDirectory.file(value))
    }
}
