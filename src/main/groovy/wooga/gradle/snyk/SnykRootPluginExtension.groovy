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

package wooga.gradle.snyk

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.util.ConfigureUtil

trait SnykRootPluginExtension implements SnykPluginExtension, SnykInstallSpec, SnykStrategySpec {
    private final Property<Boolean> autoDownload = objects.property(Boolean)

    /**
     * @return Whether to auto download the snyk executable if not found
     */
    Property<Boolean> getAutoDownload() {
        autoDownload
    }

    void setAutoDownload(Provider<Boolean> value) {
        autoDownload.set(value)
    }

    void setAutoDownload(Boolean value) {
        autoDownload.set(value)
    }

    abstract ProjectRegistrationHandler getRegisterProjectHandler()

    /**
     * Registers a new snyk project for a given File. The file can either point
     * to a directory or a file which should be understood by the snyk cli.
     *
     * @param project a file to register as a snyk project
     * @return a {@link SnykPluginExtension} object
     */
    SnykPluginExtension registerProject(File project) {
        registerProjectHandler.registerProject(project, this)
    }

    /**
     * Registers a new snyk project for a given File. The file can either point
     * to a directory or a file which should be understood by the snyk cli.
     *
     * @param project a file to register as a snyk project
     * @param action a configuration action which gets with the newly added [snyk] extension.
     * @return a {@link SnykPluginExtension} object
     */
    SnykPluginExtension registerProject(File project, Action<SnykPluginExtension> action) {
        def extension = registerProject(project)
        action.execute(extension)
        extension
    }

    /**
     * Registers a new snyk project for a given File. The file can either point
     * to a directory or a file which should be understood by the snyk cli.
     *
     * @param project a file to register as a snyk project
     * @param action a configuration closure which gets with the newly added [snyk] extension.
     * @return a {@link SnykPluginExtension} object
     */
    SnykPluginExtension registerProject(File project, Closure configure) {
        registerProject(project, ConfigureUtil.configureUsing(configure))
    }

    /**
     * Registers a single gradle sub project as a snyk project.
     * Each registered project will be configured with a custom snyk extension
     * and snyk tasks (snykTest, snykMonitor, snykReport)
     *
     * @param subProject the project to register
     * @return a {@link SnykPluginExtension} object
     */
    SnykPluginExtension registerProject(Project subProject) {
        registerProjectHandler.registerProject(subProject, this)
    }

    /**
     * Registers a single gradle sub project as a snyk project.
     * Each registered project will be configured with a custom snyk extension
     * and snyk tasks (snykTest, snykMonitor, snykReport)
     *
     * @param subProject the project to register
     * @param action a configuration action which gets with the newly added [snyk] extension on the subproject
     * @return a {@link SnykPluginExtension} object
     */
    SnykPluginExtension registerProject(Project subProject, Action<SnykPluginExtension> action) {
        def extension = registerProject(subProject)
        action.execute(extension)
        extension
    }

    /**
     * Registers a single gradle sub project as a snyk project.
     * Each registered project will be configured with a custom snyk extension
     * and snyk tasks (snykTest, snykMonitor, snykReport)
     *
     * @param subProject the project to register
     * @param configure configuration closure which gets with the newly added [snyk] extension on the subproject
     * @return a {@link SnykPluginExtension} object
     */
    SnykPluginExtension registerProject(Project subProject, Closure configure) {
        registerProject(subProject, ConfigureUtil.configureUsing(configure))
    }

    /**
     * Registers multiple subprojects provided as a {@code Iteratble<Project>}.
     * The method loops over the provided projects and calls {@link #registerProject}
     * @param subProjects the projects to register
     */
    void registerProject(Iterable<Project> subProjects) {
        subProjects.each { registerProject(it) }
    }

    /**
     * Registers multiple subprojects provided as a {@code Iteratble<Project>}.
     * The method loops over the provided projects and calls {@link #registerProject}
     *
     * @param subProjects the projects to register
     * @param configure a configuration action which gets called for each provided subproject
     */
    void registerProject(Iterable<Project> subProjects, Action<SnykPluginExtension> action) {
        subProjects.each {
            registerProject(it, action)
        }
    }

    /**
     * Registers multiple subprojects provided as a {@code Iteratble<Project>}.
     * The method loops over the provided projects and calls {@link #registerProject}
     *
     * @param subProjects the projects to register
     * @param configure a configuration closure which gets called for each provided subproject
     */
    void registerProject(Iterable<Project> subProjects, Closure configure) {
        subProjects.each {
            registerProject(it, configure)
        }
    }

    /**
     * Registers all gradle sub projects as snyk projects.
     * The method loops over all gradle sub projects and calls
     * {@link registerProject}
     *
     * @see SnykRootPluginExtension.#registerProject
     */
    void registerAllSubProjects() {
        project.subprojects { Project it ->
            registerProject(it)
        }
    }

    /**
     * Registers all gradle sub projects as snyk projects.
     * The method loops over all gradle sub projects and calls
     * {@code registerProject} with the given {@code Action} object.
     *
     * @param action a configuration action which gets called for each subproject
     *
     * @see SnykRootPluginExtension.#registerProject
     */
    void registerAllSubProjects(Action<SnykPluginExtension> action) {
        project.subprojects { Project it ->
            registerProject(it, action)
        }
    }

    /**
     * Registers all gradle sub projects as snyk projects.
     * The method loops over all gradle sub projects and calls
     * {@code registerProject} with the given {@code Closure} object.
     *
     * @param configure a configuration closure which gets called for each subproject
     *
     * @see SnykRootPluginExtension.#registerProject
     */
    void registerAllSubProjects(Closure configure) {
        /* Why not calling `registerAllSubProjects(ConfigureUtil.configureUsing(configure))`?
           This would put the scope of the closure into the wrong extension. We either let
           the actual working implementation of `registerProject(Project, Closure)` convert it,
           or we would need to adjust the closure delegate manually somewhere in our code.
           I think it is cleaner to have some reduntant code over the alternatives.
        */
        project.subprojects { Project it ->
            registerProject(it, configure)
        }
    }

    @Override
    Property<String> getExecutableName() {
        return SnykInstallSpec.super.getExecutableName()
    }

    @Override
    void setExecutableName(Provider<String> value) {
        SnykInstallSpec.super.setExecutableName(value)
    }

    @Override
    void setExecutableName(String value) {
        SnykInstallSpec.super.setExecutableName(value)
    }
}
