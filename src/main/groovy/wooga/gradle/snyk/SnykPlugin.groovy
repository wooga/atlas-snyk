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
import org.gradle.api.logging.LogLevel
import org.gradle.api.logging.Logging
import org.slf4j.Logger
import wooga.gradle.snyk.cli.BusinessCriticalityOption
import wooga.gradle.snyk.cli.EnvironmentOption
import wooga.gradle.snyk.cli.FailOnOption
import wooga.gradle.snyk.cli.LifecycleOption
import wooga.gradle.snyk.cli.SeverityThresholdOption
import wooga.gradle.snyk.cli.SnykMonitorArgumentsSpec
import wooga.gradle.snyk.cli.SnykTestArgumentSpec
import wooga.gradle.snyk.cli.VulnerablePathsOption
import wooga.gradle.snyk.internal.DefaultSnykPluginExtension
import wooga.gradle.snyk.tasks.Monitor
import wooga.gradle.snyk.tasks.SnykInstall
import wooga.gradle.snyk.tasks.SnykTask
import wooga.gradle.snyk.tasks.Test

class SnykPlugin implements Plugin<Project> {

    static Logger logger = Logging.getLogger(SnykPlugin)

    static String EXTENSION_NAME = "snyk"

    @Override
    void apply(Project project) {
        def tasks = project.tasks
        def extension = createAndConfigureExtension(project)


        tasks.withType(Test).configureEach {
            mapExtensionPropertiesToTestTask(it, extension)
        }

        tasks.withType(Monitor).configureEach {
            mapExtensionPropertiesToTestTask(it, extension)
            mapExtensionPropertiesToMonitorTask(it, extension)
        }

        tasks.withType(SnykInstall).configureEach {installTask ->
            installTask.installationDir.convention(extension.snykPath)
            installTask.executableName.convention(extension.executable)
            installTask.snykVersion.convention(extension.snykVersion)
        }
        def snykInstall = tasks.register("snykInstall", SnykInstall)

        tasks.withType(SnykTask).configureEach {
            dependsOn(snykInstall)
            it.executable.convention(extension.executable)
            it.snykPath.convention(extension.snykPath)
            it.token.convention(extension.token)
            it.token.convention(extension.token)
            it.debug.convention(extension.debug)
            it.insecure.convention(extension.insecure)
            it.logFile.convention(project.layout.buildDirectory.file("logs/${it.name}.log"))
        }
    }

    private static mapExtensionPropertiesToTestTask(SnykTestArgumentSpec task, SnykPluginExtension extension) {
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

    private static mapExtensionPropertiesToMonitorTask(SnykMonitorArgumentsSpec task, SnykPluginExtension extension) {
        task.trustPolicies.convention(extension.trustPolicies)
        task.projectEnvironment.convention(extension.projectEnvironment)
        task.projectLifecycle.convention(extension.projectLifecycle)
        task.projectBusinessCriticality.convention(extension.projectBusinessCriticality)
        task.projectTags.convention(extension.projectTags)
    }

    protected static SnykPluginExtension createAndConfigureExtension(Project project) {
        def extension = project.extensions.create(SnykPluginExtension, EXTENSION_NAME, DefaultSnykPluginExtension, project)
        def snykDefaultInstallDir= project.layout.dir(extension.snykVersion.map {version ->
            new File(project.gradle.gradleUserHomeDir, "atlas-snyk/${version}")
        })

        extension.executable.convention(SnykConventions.executable.getStringValueProvider(project))
        extension.snykVersion.convention(SnykConventions.snykVersion.getStringValueProvider(project))
        extension.snykPath.convention(SnykConventions.snykPath.getDirectoryValueProvider(project).orElse(snykDefaultInstallDir))
        extension.token.convention(SnykConventions.token.getStringValueProvider(project))

        extension.insecure.convention(SnykConventions.insecure.getBooleanValueProvider(project))
        extension.debug.convention(project.gradle.startParameter.logLevel == LogLevel.DEBUG)

        extension.allProjects.convention(SnykConventions.allProjects.getBooleanValueProvider(project))
        extension.detectionDepth.convention(SnykConventions.detectionDepth.getStringValueProvider(project).map({ Integer.parseInt(it) }))
        extension.exclude.convention(SnykConventions.exclude.getStringValueProvider(project).map({
            it.trim().split(',')
        }).map({ it.toList().collect { project.layout.projectDirectory.dir(it.trim()) } }))
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
        extension
    }
}
