package wooga.gradle.snyk.cli.commands


import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.Internal
import wooga.gradle.OptionMapper

import wooga.gradle.snyk.cli.SnykProjectSpec
import wooga.gradle.snyk.cli.options.ProjectOption

/**
 * Base spec for snyk commands
 */
trait ProjectCommandSpec implements SnykProjectSpec, OptionMapper<ProjectOption> {

    @Internal
    ProviderFactory getProviderFactory() {
        getProviders()
    }

    @Override
    String getOption(ProjectOption option) {
        def value = null

        switch (option) {

            case ProjectOption.scanAllUnmanaged:
                if (scanAllUnmanaged.present && scanAllUnmanaged.get()) {
                    value = true
                }
                break

            case ProjectOption.allSubProjects:
                if (allSubProjects.present && allSubProjects.get()) {
                    value = true
                }
                break

            case ProjectOption.subProject:
                if (subProject.present) {
                    value = subProject.get()
                }
                break

            case ProjectOption.reachable:
                if (reachable.present && reachable.get()) {
                    value = true
                }
                break

            case ProjectOption.reachableTimeout:
                if (reachableTimeout.present) {
                    value = reachableTimeout.get()
                }
                break

            case ProjectOption.strictOutOfSync:
                if (strictOutOfSync.present && strictOutOfSync.get()) {
                    value = true
                }
                break

            case ProjectOption.yarnWorkspaces:
                if (yarnWorkspaces.present && yarnWorkspaces.get()) {
                    value = true
                }
                break

            case ProjectOption.assetsProjectName:
                if (assetsProjectName.present && assetsProjectName.get()) {
                    value = true
                }
                break

            case ProjectOption.packagesFolder:
                if (packagesFolder.present) {
                    value = packagesFolder.get().asFile
                }
                break

            case ProjectOption.projectNamePrefix:
                if (projectNamePrefix.present) {
                    value = projectNamePrefix.get()
                }
                break

            case ProjectOption.skipUnresolved:
                if (skipUnresolved.present && skipUnresolved.get()) {
                    value = true
                }
                break

            case ProjectOption.command:
                if (command.present) {
                    value = command.get()
                }
                break

            case ProjectOption.configurationMatching:
                if (configurationMatching.present) {
                    value = configurationMatching.get()
                }
                break

            case ProjectOption.configurationAttributes:
                if (configurationAttributes.present && !configurationAttributes.get().isEmpty()) {
                    value = configurationAttributes.get()
                }
                break

            case ProjectOption.initScript:
                if (initScript.present) {
                    value = initScript.get().asFile
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
