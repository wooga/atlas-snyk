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

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.logging.LogLevel
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.TaskCollection
import org.gradle.api.tasks.TaskProvider
import org.slf4j.Logger
import wooga.gradle.snyk.cli.*
import wooga.gradle.snyk.cli.commands.MonitorProjectCommandSpec
import wooga.gradle.snyk.cli.commands.TestProjectCommandSpec
import wooga.gradle.snyk.internal.DefaultSnykPluginExtension
import wooga.gradle.snyk.tasks.Monitor
import wooga.gradle.snyk.tasks.SnykInstall
import wooga.gradle.snyk.tasks.SnykTask
import wooga.gradle.snyk.tasks.Test

class SnykPlugin implements Plugin<Project> {

    static Logger logger = Logging.getLogger(SnykPlugin)

    static String EXTENSION_NAME = "snyk"
    static String INSTALL_TASK_NAME = "snykInstall"
    static String TEST_TASK_NAME = "snykTest"
    static String MONITOR_TASK_NAME = "snykMonitor"

    static final String MONITOR_CHECK = "monitor_check"
    static final String TEST_CHECK = "test_check"
    static final String MONITOR_PUBLISH = "monitor_publish"

    @Override
    void apply(Project project) {
        def tasks = project.tasks
        // Create the extension
        def extension = createAndConfigureExtension(project)
        // Map the base task properties common to all snyk tasks
        mapExtensionPropertiesToBaseTask(extension, project)
        // Map the properties specific to certain tasks
        tasks.withType(Test).configureEach {
            mapExtensionPropertiesToTestTask(it, extension)
        }
        tasks.withType(Monitor).configureEach {
            mapExtensionPropertiesToTestTask(it, extension)
            mapExtensionPropertiesToMonitorTask(it, extension)
        }
        // Register an install task (to be used to install the snyk binary if need be)
        registerSnykTasks(project, extension)
    }

    protected static SnykPluginExtension createAndConfigureExtension(Project project) {
        def extension = project.extensions.create(SnykPluginExtension, EXTENSION_NAME, DefaultSnykPluginExtension, project)

        // TODO: Move to conventions?
        extension.autoDownload.convention(false)
        extension.autoUpdate.convention(true)

        extension.version.convention(SnykConventions.version.getStringValueProvider(project))
        extension.executableName.convention(SnykConventions.executableName.getStringValueProvider(project))
        extension.installationDir.convention(SnykConventions.installationDir.getDirectoryValueProvider(project).orElse(
                project.layout.dir(extension.version.map { version ->
                    new File(project.gradle.gradleUserHomeDir, "atlas-snyk/${version}")
                })
        ))

        // If the convention for the snyk path is null, then it will use the convention provided by the
        // install task if autoDownloadSnykCli is true
        extension.snykPath.convention(SnykConventions.snykPath.getDirectoryValueProvider(project).
                orElse(extension.autoDownload.flatMap({
                    return it ? extension.installationDir : null
                })
                )
        )

        extension.token.convention(SnykConventions.token.getStringValueProvider(project))

        extension.insecure.convention(SnykConventions.insecure.getBooleanValueProvider(project))
        extension.debug.convention(project.gradle.startParameter.logLevel == LogLevel.DEBUG)

        extension.allProjects.convention(SnykConventions.allProjects.getBooleanValueProvider(project))
        extension.detectionDepth.convention(SnykConventions.detectionDepth.getStringValueProvider(project).map({ Integer.parseInt(it) }))
        extension.exclude.convention(SnykConventions.exclude.getStringValueProvider(project).map({
            it.trim().split(',')
        }).map({ it.toList().collect { project.file(it.trim()) } }))
        extension.pruneRepeatedSubDependencies.convention(SnykConventions.pruneRepeatedSubDependencies.getBooleanValueProvider(project))
        extension.printDependencies.convention(SnykConventions.printDependencies.getBooleanValueProvider(project))
        extension.remoteRepoUrl.convention(SnykConventions.remoteRepoUrl.getStringValueProvider(project))
        extension.includeDevelopmentDependencies.convention(SnykConventions.includeDevelopmentDependencies.getBooleanValueProvider(project))
        extension.orgName.convention(SnykConventions.orgName.getStringValueProvider(project))
        extension.packageFile.convention(SnykConventions.packageFile.getFileValueProvider(project))
        extension.ignorePolicy.convention(SnykConventions.ignorePolicy.getBooleanValueProvider(project))
        extension.showVulnerablePaths.convention(SnykConventions.showVulnerablePaths.getStringValueProvider(project).map({
            VulnerablePathsOption.valueOf(it.trim())
        }))
        extension.projectName.convention(SnykConventions.projectName.getStringValueProvider(project))
        extension.targetReference.convention(SnykConventions.targetReference.getStringValueProvider(project))
        extension.policyPath.convention(SnykConventions.policyPath.getFileValueProvider(project))
        extension.printJson.convention(SnykConventions.printJson.getBooleanValueProvider(project))
        extension.jsonOutputPath.convention(SnykConventions.jsonOutputPath.getFileValueProvider(project))
        extension.printSarif.convention(SnykConventions.printSarif.getBooleanValueProvider(project))
        extension.sarifOutputPath.convention(SnykConventions.sarifOutputPath.getFileValueProvider(project))
        extension.severityThreshold.convention(SnykConventions.severityThreshold.getStringValueProvider(project).map({
            SeverityThresholdOption.valueOf(it)
        }))
        extension.failOn.convention(SnykConventions.failOn.getStringValueProvider(project).map({
            FailOnOption.valueOf(it)
        }))
        extension.compilerArguments.convention(SnykConventions.compilerArguments.getStringValueProvider(project).map({
            it.trim().split(" ").toList()
        }))
        extension.trustPolicies.convention(SnykConventions.trustPolicies.getBooleanValueProvider(project))
        extension.projectEnvironment.convention(SnykConventions.projectEnvironment.getStringValueProvider(project).map({
            it.trim().split(',')
        }).map({
            it.toList().collect {
                EnvironmentOption.valueOf(it.trim())
            }
        }))
        extension.projectLifecycle.convention(SnykConventions.projectLifecycle.getStringValueProvider(project).map({
            it.trim().split(',')
        }).map({
            it.toList().collect {
                LifecycleOption.valueOf(it.trim())
            }
        }))
        extension.projectBusinessCriticality.convention(SnykConventions.projectBusinessCriticality.getStringValueProvider(project).map({
            it.trim().split(',')
        }).map({
            it.toList().collect {
                BusinessCriticalityOption.valueOf(it.trim())
            }
        }))
        extension.projectTags.convention(SnykConventions.projectTags.getStringValueProvider(project).map({
            it.trim().split(',').collectEntries {
                def parts = it.trim().split("=")
                [(parts[0].toString().trim()): parts[1].toString().trim()]
            } as Map<String, String>
        }))

        extension.assetsProjectName.convention(SnykConventions.assetsProjectName.getBooleanValueProvider(project))
        extension.packagesFolder.convention(SnykConventions.packagesFolder.getDirectoryValueProvider(project))
        extension.projectNamePrefix.convention(SnykConventions.projectNamePrefix.getStringValueProvider(project))
        extension.strictOutOfSync.convention(SnykConventions.strictOutOfSync.getBooleanValueProvider(project))
        extension.scanAllUnmanaged.convention(SnykConventions.scanAllUnmanaged.getBooleanValueProvider(project))
        extension.reachable.convention(SnykConventions.reachable.getBooleanValueProvider(project))
        extension.reachableTimeout.convention(SnykConventions.reachableTimeout.getStringValueProvider(project).map({ Integer.parseInt(it) }))
        extension.subProject.convention(SnykConventions.subProject.getStringValueProvider(project))
        extension.allSubProjects.convention(SnykConventions.allSubProjects.getBooleanValueProvider(project))
        extension.configurationMatching.convention(SnykConventions.configurationMatching.getStringValueProvider(project))
        extension.configurationAttributes.convention(SnykConventions.configurationAttributes.getStringValueProvider(project).map({
            it.trim().split(',') as List<String>
        }))
        extension.command.convention(SnykConventions.command.getStringValueProvider(project))
        extension.initScript.convention(SnykConventions.initScript.getFileValueProvider(project))
        extension.skipUnresolved.convention(SnykConventions.skipUnresolved.getBooleanValueProvider(project))
        extension.yarnWorkspaces.convention(SnykConventions.yarnWorkspaces.getBooleanValueProvider(project))

        extension.strategies.convention(SnykConventions.strategies.getStringValueProvider(project).map({
            it.split(",").collect({ it.trim() })
        }))

        extension.checkTaskName.convention(SnykConventions.checkTaskName.getStringValueProvider(project))
        extension.publishTaskName.convention(SnykConventions.publishTaskName.getStringValueProvider(project))

        extension

    }

    private static mapExtensionPropertiesToBaseTask(extension, project) {
        project.tasks.withType(SnykTask).configureEach {
            it.logToStdout.convention(it.logger.infoEnabled)
            it.workingDirectory.convention(extension.workingDirectory)
            it.executableName.convention(extension.executableName)
            it.snykPath.convention(extension.snykPath)
            it.token.convention(extension.token)
            it.debug.convention(extension.debug)
            it.insecure.convention(extension.insecure)
            it.logFile.convention(project.layout.buildDirectory.file("logs/${it.name}.log"))
        }
    }

    private static mapExtensionPropertiesToTestTask(TestProjectCommandSpec task, SnykPluginExtension extension) {

        task.allProjects.convention(extension.allProjects)
        task.detectionDepth.convention(extension.detectionDepth)
        task.exclude.convention(extension.exclude)
        task.pruneRepeatedSubDependencies.convention(extension.pruneRepeatedSubDependencies)
        task.printDependencies.convention(extension.printDependencies)
        task.remoteRepoUrl.convention(extension.remoteRepoUrl)
        task.includeDevelopmentDependencies.convention(extension.includeDevelopmentDependencies)
        task.orgName.convention(extension.orgName)
        task.packageFile.convention(extension.packageFile)
        task.ignorePolicy.convention(extension.ignorePolicy)
        task.showVulnerablePaths.convention(extension.showVulnerablePaths)
        task.projectName.convention(extension.projectName)
        task.targetReference.convention(extension.targetReference)
        task.policyPath.convention(extension.policyPath)
        task.printJson.convention(extension.printJson)
        task.jsonOutputPath.convention(extension.jsonOutputPath)
        task.printSarif.convention(extension.printSarif)
        task.sarifOutputPath.convention(extension.sarifOutputPath)
        task.severityThreshold.convention(extension.severityThreshold)
        task.failOn.convention(extension.failOn)
        task.compilerArguments.convention(extension.compilerArguments)

        task.strictOutOfSync.convention(extension.strictOutOfSync)

        task.assetsProjectName.convention(extension.assetsProjectName)
        task.packagesFolder.convention(extension.packagesFolder)
        task.projectNamePrefix.convention(extension.projectNamePrefix)

        task.subProject.convention(extension.subProject)
        task.allSubProjects.convention(extension.allSubProjects)
        task.configurationMatching.convention(extension.configurationMatching)
        task.configurationAttributes.convention(extension.configurationAttributes)
        task.reachable.convention(extension.reachable)
        task.reachableTimeout.convention(extension.reachableTimeout)
        task.initScript.convention(extension.initScript)

        task.command.convention(extension.command)
        task.skipUnresolved.convention(extension.skipUnresolved)

        task.yarnWorkspaces.convention(extension.yarnWorkspaces)
        task.scanAllUnmanaged.convention(extension.scanAllUnmanaged)
    }

    private static mapExtensionPropertiesToMonitorTask(MonitorProjectCommandSpec task, SnykPluginExtension extension) {
        task.trustPolicies.convention(extension.trustPolicies)
        task.projectEnvironment.convention(extension.projectEnvironment)
        task.projectLifecycle.convention(extension.projectLifecycle)
        task.projectBusinessCriticality.convention(extension.projectBusinessCriticality)
        task.projectTags.convention(extension.projectTags)
    }

    private static void registerSnykTasks(Project project, SnykPluginExtension extension) {
        def snykInstall = project.tasks.register(INSTALL_TASK_NAME, SnykInstall)
        project.tasks.register(TEST_TASK_NAME, Test)
        project.tasks.register(MONITOR_TASK_NAME, Monitor)

        project.tasks.withType(SnykInstall).configureEach { installTask ->
            installTask.installationDir.convention(extension.installationDir)
            installTask.executableName.convention(extension.executableName)
            installTask.version.convention(extension.version)
        }

        project.tasks.withType(SnykTask).configureEach {
            if (extension.autoUpdate.get()) {
                it.dependsOn(snykInstall)
            }
        }

        project.afterEvaluate {
            def strategy = extension.strategies.getOrElse([])
            if (strategy.size() > 0) {
                if (strategy.contains(MONITOR_PUBLISH)) {
                    hookSnykTask(project.tasks.withType(Monitor), project.tasks.named(extension.publishTaskName.get()))
                }
                if (strategy.contains(MONITOR_CHECK)) {
                    hookSnykTask(project.tasks.withType(Monitor), project.tasks.named(extension.checkTaskName.get()))
                }
                if (strategy.contains(TEST_CHECK)) {
                    hookSnykTask(project.tasks.withType(Test), project.tasks.named(extension.checkTaskName.get()))
                }
            }
        }
    }

    private static void hookSnykTask(TaskCollection<Task> snykTasks, TaskProvider<Task> baseTask) {
        baseTask.configure({
            it.dependsOn(snykTasks)
        })
    }
}
