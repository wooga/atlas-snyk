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
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskCollection
import org.gradle.api.tasks.TaskProvider
import org.slf4j.Logger
import wooga.gradle.snyk.cli.*
import wooga.gradle.snyk.cli.commands.MonitorProjectCommandSpec
import wooga.gradle.snyk.cli.commands.TestProjectCommandSpec
import wooga.gradle.snyk.internal.DefaultSnykPluginExtension
import wooga.gradle.snyk.internal.DefaultSnykRootPluginExtension
import wooga.gradle.snyk.internal.DefaultSnykToHtmlPluginExtension
import wooga.gradle.snyk.tasks.*

class SnykPlugin implements Plugin<Project>, ProjectRegistrationHandler {

    static Logger logger = Logging.getLogger(SnykPlugin)

    static String GROUP = "snyk"
    static String EXTENSION_NAME = "snyk"
    static String INSTALL_TASK_NAME = "snykInstall"
    static String INSTALL_TO_HTML_TASK_NAME = "snykToHtmlInstall"

    static String TEST_TASK_NAME = "snykTest"
    static String REPORT_TASK_NAME = "snykReport"
    static String MONITOR_TASK_NAME = "snykMonitor"

    static final String MONITOR_CHECK = "monitor_check"
    static final String TEST_CHECK = "test_check"
    static final String REPORT_CHECK = "report_check"
    static final String MONITOR_PUBLISH = "monitor_publish"
    static final String TEST_PUBLISH = "test_publish"
    static final String REPORT_PUBLISH = "report_publish"

    private Project project

    @Override
    void apply(Project project) {
        this.project = project
        def tasks = project.tasks

        def snykToHtmlExtension = createAndConfigureSnykToHtmlExtension(project)
        def snykExtension = createAndConfigureExtension(project, snykToHtmlExtension)

        // Map the base task properties common to all snyk tasks
        project.tasks.withType(SnykTask).configureEach {
            mapExtensionPropertiesToBaseTask(it, snykExtension, project)
        }
        // Map the properties specific to certain tasks

        List<Class<SnykCheckBase>> checkTypes = new ArrayList<>()
        checkTypes << Test << Report

        checkTypes.each {
            tasks.withType(it).configureEach {
                mapExtensionPropertiesToCheckBaseTask(it, snykExtension)
                mapSnykToHtmlExtensionPropertiesToCheckBaseTask(it, snykToHtmlExtension)
            }
        }

        tasks.withType(Monitor).configureEach {
            mapExtensionPropertiesToTestTask(it, snykExtension)
            mapExtensionPropertiesToMonitorTask(it, snykExtension)
        }
        // Register an install task (to be used to install the snyk binary if need be)
        registerSnykInstall(project, snykExtension)
        registerSnykToHtmlInstall(project, snykToHtmlExtension)
        registerTaskGroupAndDescription(project)
        hookStrategyLifecycleTasks(project, snykExtension)
    }

    static void mapExtensionPropertiesToCheckBaseTask(SnykCheckBase task, SnykPluginExtension extension) {
        mapExtensionPropertiesToTestTask(task, extension)
        task.reports.sarif.required.convention(extension.sarifReportsEnabled)
        task.reports.json.required.convention(extension.jsonReportsEnabled)
        task.reports.html.required.convention(extension.htmlReportsEnabled)

        task.reports.sarif.outputLocation.convention(extension.reportsDir.file(task.name + "/" + task.name + "." + task.reports.sarif.name))
        task.reports.json.outputLocation.convention(extension.reportsDir.file(task.name + "/" + task.name + "." + task.reports.json.name))
        task.reports.html.outputLocation.convention(extension.reportsDir.file(task.name + "/" + task.name + "." + task.reports.html.name))
    }

    static void mapSnykToHtmlExtensionPropertiesToCheckBaseTask(SnykCheckBase task, SnykToHtmlPluginExtension extension) {
        task.reports.html.actionableRemediation.convention(extension.actionableRemediation)
        task.reports.html.summaryOnly.convention(extension.summaryOnly)
        task.reports.html.executableName.convention(extension.executableName)
        task.reports.html.snykPath.convention(extension.snykPath)
        task.reports.html.logFile.convention(task.logFile)
        task.reports.html.logToStdout.convention(task.logToStdout)
    }

    /**
     * Registers tasks on the sub project. We don't have to do name-mangling
     * since there's a separate namespace
     */
    @Override
    SnykPluginExtension registerProject(Project subProject, SnykRootPluginExtension parentExtension) {
        def subProjectName = parentExtension.projectName.orElse(subProject.rootProject.name).map({ it + subProject.path })
        def snykTest = subProject.tasks.register(TEST_TASK_NAME, Test)
        def snykReport = subProject.tasks.register(REPORT_TASK_NAME, Report)
        def snykMonitor = subProject.tasks.register(MONITOR_TASK_NAME, Monitor)

        def extension = subProject.extensions.create(SnykPluginExtension, EXTENSION_NAME, DefaultSnykPluginExtension, subProject, snykTest, snykMonitor, snykReport)

        mapParentExtensionToExtension(subProject, subProjectName, extension, parentExtension, snykTest, snykMonitor, snykReport)
        registerTaskGroupAndDescription(subProject)
        // We don't want subprojects to re-use custom files that the parent sets
        extension.packageFile.set(null)
        // Set the same working directory as the parent
        extension.workingDirectory.convention(parentExtension.workingDirectory)
        // Assigning this will have the sub-project flag composed with the command line arguments
        extension.subProject.set(subProject.name)
        // reset reports directory based on subproject
        extension.reportsDir.convention(SnykConventions.reportsDir.getDirectoryValueProvider(subProject))
        // register the root snykInstall task for snyk tasks in the subproject
        registerSnykInstall(subProject, parentExtension)
        registerSnykToHtmlInstall(subProject, parentExtension.snykToHtml)
        extension
    }

    /**
     * Registers tasks from a subproject onto the root project. we have to mangle the generated tasks
     * to differentiate them from the root + any others
     */
    SnykPluginExtension registerProject(File projectFile, SnykRootPluginExtension parentExtension) {
        def relativeProjectFile = project.relativePath(projectFile)
        def projectName = parentExtension.projectName.orElse(project.name).map({ it + ":" + relativeProjectFile })

        if (projectFile.isFile()) {
            logger.info("register snyk project file: ${projectFile.absolutePath}")
        } else {
            logger.info("register snyk project at path: ${projectFile.absolutePath}")
        }

        // Replace all characters gradle doesn't like in task names with '.'
        def projectTaskName = relativeProjectFile.replaceAll(/[\/\\:"<>"?*]/, ".")

        def snykTest = project.tasks.register(TEST_TASK_NAME + "." + projectTaskName, Test)
        def snykReport = project.tasks.register(REPORT_TASK_NAME + "." + projectTaskName, Report)
        def snykMonitor = project.tasks.register(MONITOR_TASK_NAME + "." + projectTaskName, Monitor)

        def extension = project.extensions.create(SnykPluginExtension, EXTENSION_NAME + "." + projectTaskName, DefaultSnykPluginExtension, project, snykTest, snykMonitor, snykReport)
        mapParentExtensionToExtension(project, projectName, extension, parentExtension, snykTest, snykMonitor, snykReport)

        if (!projectFile.exists()) {
            throw new FileNotFoundException("project file to be registered does not exist")
        }

        if (projectFile.isFile()) {
            extension.packageFile.convention(project.layout.projectDirectory.file(projectFile.absolutePath))
            extension.workingDirectory.convention(parentExtension.workingDirectory)
            extension.subProject.convention(null)
        } else {
            extension.packageFile.convention(parentExtension.packageFile)
            extension.workingDirectory.convention(projectFile.absolutePath)
            extension.subProject.convention(null)
        }
        extension.packageFile.map()
        extension
    }

    static void mapParentExtensionToExtension(
            Project project,
            Provider<String> projectName,
            SnykPluginExtension extension,
            SnykRootPluginExtension parentExtension,
            TaskProvider<Test> snykTest,
            TaskProvider<Monitor> snykMonitor,
            TaskProvider<Report> snykReport) {
        ([snykTest, snykReport, snykMonitor] as List<TaskProvider<SnykTask>>).each {
            it.configure {
                mapExtensionPropertiesToBaseTask(it, extension, project)
            }
        }

        snykTest.configure({
            mapExtensionPropertiesToCheckBaseTask(it, extension)
            mapSnykToHtmlExtensionPropertiesToCheckBaseTask(it, parentExtension.snykToHtml)
        })
        snykReport.configure({
            mapExtensionPropertiesToCheckBaseTask(it, extension)
            mapSnykToHtmlExtensionPropertiesToCheckBaseTask(it, parentExtension.snykToHtml)
        })

        snykMonitor.configure {
            mapExtensionPropertiesToTestTask(it, extension)
            mapExtensionPropertiesToMonitorTask(it, extension)
        }

        // TODO: Refactor into looping through all the shared properties since this approach is bug-prone
        extension.projectName.convention(projectName)
        extension.executableName.convention(parentExtension.executableName)
        extension.snykPath.convention(parentExtension.snykPath)
        extension.token.convention(parentExtension.token)
        extension.insecure.convention(parentExtension.insecure)
        extension.debug.convention(parentExtension.debug)
        extension.allProjects.convention(parentExtension.allProjects)
        extension.detectionDepth.convention(parentExtension.detectionDepth)
        extension.exclude.convention(parentExtension.exclude)
        extension.pruneRepeatedSubDependencies.convention(parentExtension.pruneRepeatedSubDependencies)
        extension.printDependencies.convention(parentExtension.printDependencies)
        extension.remoteRepoUrl.convention(parentExtension.remoteRepoUrl)
        extension.includeDevelopmentDependencies.convention(parentExtension.includeDevelopmentDependencies)
        extension.orgName.convention(parentExtension.orgName)
        extension.ignorePolicy.convention(parentExtension.ignorePolicy)
        extension.showVulnerablePaths.convention(parentExtension.showVulnerablePaths)
        extension.targetReference.convention(parentExtension.targetReference)
        extension.policyPath.convention(parentExtension.policyPath)
        extension.printJson.convention(parentExtension.printJson)
        extension.printSarif.convention(parentExtension.printSarif)
        extension.severityThreshold.convention(parentExtension.severityThreshold)
        extension.failOn.convention(parentExtension.failOn)
        extension.compilerArguments.convention(parentExtension.compilerArguments)
        extension.trustPolicies.convention(parentExtension.trustPolicies)
        extension.projectEnvironment.convention(parentExtension.projectEnvironment)
        extension.projectLifecycle.convention(parentExtension.projectLifecycle)
        extension.projectBusinessCriticality.convention(parentExtension.projectBusinessCriticality)
        extension.projectTags.set(parentExtension.projectTags)
        extension.assetsProjectName.convention(parentExtension.assetsProjectName)
        extension.packagesFolder.convention(parentExtension.packagesFolder)
        extension.projectNamePrefix.convention(parentExtension.projectNamePrefix)
        extension.strictOutOfSync.convention(parentExtension.strictOutOfSync)
        extension.scanAllUnmanaged.convention(parentExtension.scanAllUnmanaged)
        extension.reachable.convention(parentExtension.reachable)
        extension.reachableTimeout.convention(parentExtension.reachableTimeout)
        extension.subProject.convention(parentExtension.subProject)
        extension.allSubProjects.convention(parentExtension.allSubProjects)
        extension.configurationMatching.convention(parentExtension.configurationMatching)
        extension.configurationAttributes.convention(parentExtension.configurationAttributes)
        extension.command.convention(parentExtension.command)
        extension.initScript.convention(parentExtension.initScript)
        extension.skipUnresolved.convention(parentExtension.skipUnresolved)
        extension.yarnWorkspaces.convention(parentExtension.yarnWorkspaces)

        extension.reportsDir.convention(parentExtension.reportsDir)
        extension.jsonReportsEnabled.convention(parentExtension.jsonReportsEnabled)
        extension.sarifReportsEnabled.convention(parentExtension.sarifReportsEnabled)
        extension.htmlReportsEnabled.convention(parentExtension.htmlReportsEnabled)
        extension.ignoreExitValue.convention(parentExtension.ignoreExitValue)

        extension
    }

    protected static SnykToHtmlPluginExtension createAndConfigureSnykToHtmlExtension(Project project) {
        def snykToHtmlInstall = project.tasks.register(INSTALL_TO_HTML_TASK_NAME, SnykToHtmlInstall)
        def extension = project.extensions.create(SnykToHtmlPluginExtension, "snykToHtml", DefaultSnykToHtmlPluginExtension, snykToHtmlInstall)

        extension.version.convention(SnykConventions.snykToHtmlVersion.getStringValueProvider(project))
        extension.executableName.convention(SnykConventions.snykToHtmlExecutableName.getStringValueProvider(project))
        extension.installationDir.convention(SnykConventions.snykToHtmlInstallationDir.getDirectoryValueProvider(project).orElse(
                project.layout.dir(extension.version.map { version ->
                    new File(project.gradle.gradleUserHomeDir, "atlas-snyk/snyk-to-html/${version}")
                })
        ))

        extension.summaryOnly.convention(SnykConventions.snykToHtmlSummaryOnly.getBooleanValueProvider(project))
        extension.actionableRemediation.convention(SnykConventions.snykToHtmlActionableRemediation.getBooleanValueProvider(project))
        extension.autoDownload.convention(SnykConventions.autoDownload.getBooleanValueProvider(project))
        // If the convention for the snyk path is null, then it will use the convention provided by the
        // install task if autoDownloadSnykCli is true
        extension.snykPath.convention(SnykConventions.snykToHtmlPath.getDirectoryValueProvider(project).
                orElse(extension.autoDownload.flatMap({
                    return it ? extension.installationDir : null
                })
                )
        )

        extension
    }

    protected SnykRootPluginExtension createAndConfigureExtension(Project project, SnykToHtmlPluginExtension snykToHtmlPluginExtension) {
        def snykInstall = project.tasks.register(INSTALL_TASK_NAME, SnykInstall)
        def snykTest = project.tasks.register(TEST_TASK_NAME, Test)
        def snykReport = project.tasks.register(REPORT_TASK_NAME, Report)
        def snykMonitor = project.tasks.register(MONITOR_TASK_NAME, Monitor)

        def extension = project.extensions.create(SnykRootPluginExtension, EXTENSION_NAME, DefaultSnykRootPluginExtension, project, snykInstall, snykTest, snykMonitor, snykReport, snykToHtmlPluginExtension, this)
        snykToHtmlPluginExtension.autoDownload.convention(extension.autoDownload)
        extension.autoDownload.convention(SnykConventions.autoDownload.getBooleanValueProvider(project))

        extension.workingDirectory.set(project.layout.projectDirectory.asFile.absolutePath)
        extension.version.convention(SnykConventions.version.getStringValueProvider(project))
        extension.executableName.convention(SnykConventions.executableName.getStringValueProvider(project))
        extension.installationDir.convention(SnykConventions.installationDir.getDirectoryValueProvider(project).orElse(
                project.layout.dir(extension.version.map { version ->
                    new File(project.gradle.gradleUserHomeDir, "atlas-snyk/snyk/${version}")
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
        extension.debug.convention(SnykConventions.debug.getBooleanValueProvider(project).orElse(project.gradle.startParameter.logLevel == LogLevel.DEBUG))

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
        extension.packageManager.convention(SnykConventions.packageManager.getStringValueProvider(project))
        extension.ignorePolicy.convention(SnykConventions.ignorePolicy.getBooleanValueProvider(project))
        extension.showVulnerablePaths.convention(SnykConventions.showVulnerablePaths.getStringValueProvider(project).map({
            VulnerablePathsOption.valueOf(it.trim())
        }))
        extension.projectName.convention(SnykConventions.projectName.getStringValueProvider(project))
        extension.targetReference.convention(SnykConventions.targetReference.getStringValueProvider(project))
        extension.policyPath.convention(SnykConventions.policyPath.getFileValueProvider(project))
        extension.printJson.convention(SnykConventions.printJson.getBooleanValueProvider(project))
        extension.printSarif.convention(SnykConventions.printSarif.getBooleanValueProvider(project))
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

        extension.reportsDir.convention(SnykConventions.reportsDir.getDirectoryValueProvider(project))
        extension.jsonReportsEnabled.convention(SnykConventions.jsonReportsEnabled.getBooleanValueProvider(project))
        extension.sarifReportsEnabled.convention(SnykConventions.sarifReportsEnabled.getBooleanValueProvider(project))
        extension.htmlReportsEnabled.convention(SnykConventions.htmlReportsEnabled.getBooleanValueProvider(project))
        extension.ignoreExitValue.convention(SnykConventions.ignoreExitValue.getBooleanValueProvider(project))
        extension
    }

    private static mapExtensionPropertiesToBaseTask(SnykTask task, SnykPluginExtension extension, Project project) {
        task.logToStdout.convention(project.logger.isEnabled(LogLevel.INFO))
        task.workingDirectory.convention(extension.workingDirectory)
        task.executableName.convention(extension.executableName)
        task.snykPath.convention(extension.snykPath)
        task.token.convention(extension.token)
        task.debug.convention(extension.debug)
        task.insecure.convention(extension.insecure)
        task.logFile.convention(project.layout.buildDirectory.file("logs/${task.name}.log"))
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
        task.packageManager.convention(extension.packageManager)
        task.ignorePolicy.convention(extension.ignorePolicy)
        task.showVulnerablePaths.convention(extension.showVulnerablePaths)
        task.projectName.convention(extension.projectName)
        task.targetReference.convention(extension.targetReference)
        task.policyPath.convention(extension.policyPath)
        task.printJson.convention(extension.printJson)
        task.printSarif.convention(extension.printSarif)
        task.severityThreshold.convention(extension.severityThreshold)
        task.failOn.convention(extension.failOn)
        task.compilerArguments.convention(extension.compilerArguments)
        task.ignoreExitValue.convention(extension.ignoreExitValue)

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

    private static void registerTaskGroupAndDescription(Project project) {
        project.tasks.withType(SnykInstall).configureEach({
            it.group = GROUP
        })

        project.tasks.withType(Test).configureEach({
            it.group = GROUP
            it.description = "run snyk monitor on project"
        })

        project.tasks.withType(Monitor).configureEach({
            it.group = GROUP
            it.description = "run snyk test on project"
        })

        project.tasks.withType(Report).configureEach({
            it.group = GROUP
            it.description = "generate snyk report"
        })
    }

    private static void registerSnykToHtmlInstall(Project project, SnykToHtmlPluginExtension extension) {
        project.tasks.withType(SnykToHtmlInstall).configureEach { installTask ->
            installTask.description = "Install snyk-to-html helper tool"
            installTask.installationDir.convention(extension.installationDir)
            installTask.executableName.convention(extension.executableName)
            installTask.version.convention(extension.version)
        }

        project.tasks.withType(SnykCheckBase).configureEach({
            if (extension.autoDownload.get() && it.reports.html.required.get()) {
                it.dependsOn(extension.snykToHtmlInstall)
            }
        })
    }

    private static void registerSnykInstall(Project project, SnykRootPluginExtension extension) {
        project.tasks.withType(SnykInstall).configureEach { installTask ->
            installTask.description = "Install snyk cli"
            installTask.installationDir.convention(extension.installationDir)
            installTask.executableName.convention(extension.executableName)
            installTask.version.convention(extension.version)
        }

        project.tasks.withType(SnykTask).configureEach {
            if (extension.autoDownload.get()) {
                it.dependsOn(extension.snykInstall)
            }
        }
    }

    private static void hookStrategyLifecycleTasks(Project project, SnykRootPluginExtension extension) {
        def snykTestAll = project.tasks.register("snykTestAll")
        def snykMonitorAll = project.tasks.register("snykMonitorAll")
        def snykReportAll = project.tasks.register("snykReportAll")

        project.afterEvaluate {
            def allSnykTestTasks = project.allprojects.collect { it.tasks.withType(Test) }
            def allSnykMonitorTasks = project.allprojects.collect {
                it.tasks.withType(Monitor)
            }
            def allSnyReportTasks = project.allprojects.collect { it.tasks.withType(Report) }

            hookSnykTask(allSnykTestTasks, snykTestAll)
            hookSnykTask(allSnykMonitorTasks, snykMonitorAll)
            hookSnykTask(allSnyReportTasks, snykReportAll)

            def strategy = extension.strategies.getOrElse([])
            if (strategy.size() > 0) {
                if (strategy.contains(MONITOR_PUBLISH)) {
                    hookSnykTask(allSnykMonitorTasks, project.tasks.named(extension.publishTaskName.get()))
                }
                if (strategy.contains(TEST_PUBLISH)) {
                    hookSnykTask(allSnykTestTasks, project.tasks.named(extension.publishTaskName.get()))
                }
                if (strategy.contains(REPORT_PUBLISH)) {
                    hookSnykTask(allSnyReportTasks, project.tasks.named(extension.publishTaskName.get()))
                }

                if (strategy.contains(MONITOR_CHECK)) {
                    hookSnykTask(allSnykMonitorTasks, project.tasks.named(extension.checkTaskName.get()))
                }
                if (strategy.contains(TEST_CHECK)) {
                    hookSnykTask(allSnykTestTasks, project.tasks.named(extension.checkTaskName.get()))
                }
                if (strategy.contains(REPORT_CHECK)) {
                    hookSnykTask(allSnyReportTasks, project.tasks.named(extension.checkTaskName.get()))
                }
            }
        }
    }

    private static void hookSnykTask(TaskCollection<Task> snykTasks, TaskProvider<Task> baseTask) {
        baseTask.configure({
            it.dependsOn(snykTasks)
        })
    }

    private static void hookSnykTask(List<TaskCollection<Task>> snykTasks, TaskProvider<Task> baseTask) {
        baseTask.configure({ Task task ->
            snykTasks.each {
                task.dependsOn(it)
            }
        })
    }
}
