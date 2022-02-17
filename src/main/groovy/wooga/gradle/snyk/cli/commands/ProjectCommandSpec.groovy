package wooga.gradle.snyk.cli.commands

import com.wooga.gradle.BaseSpec
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.options.Option
import wooga.gradle.OptionMapper
import wooga.gradle.snyk.cli.options.ProjectOption

/**
 * Base spec for snyk commands
 */
trait ProjectCommandSpec extends BaseSpec implements OptionMapper<ProjectOption> {

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

    private final Property<Boolean> strictOutOfSync = objects.property(Boolean)

    @Input
    @Optional
    @Option(option = "strict-out-of-sync", description = """
    Control testing out of sync lockfiles.
    """)
    Property<Boolean> getStrictOutOfSync() {
        strictOutOfSync
    }

    void setStrictOutOfSync(Provider<Boolean> value) {
        strictOutOfSync.set(value)
    }

    void setStrictOutOfSync(Boolean value) {
        strictOutOfSync.set(value)
    }

    private final Property<Boolean> yarnWorkspaces = objects.property(Boolean)

    @Input
    @Optional
    @Option(option = "yarn-workspaces", description = """
    Detect and scan yarn workspaces. You can specify how many sub-directories to search using
    --detection-depth and exclude directories and files using --exclude.
    """)
    Property<Boolean> getYarnWorkspaces() {
        yarnWorkspaces
    }

    void setYarnWorkspaces(Provider<Boolean> value) {
        yarnWorkspaces.set(value)
    }

    void setYarnWorkspaces(Boolean value) {
        yarnWorkspaces.set(value)
    }

    private final Property<Boolean> scanAllUnmanaged = objects.property(Boolean)

    @Input
    @Optional
    @Option(option = "scan-all-unmanaged", description = """
    Auto-detect maven jars, aars, and wars in given directory. To test individually use
    --file=<JAR_FILE_NAME>.
    """)
    Property<Boolean> getScanAllUnmanaged() {
        scanAllUnmanaged
    }

    void setScanAllUnmanaged(Provider<Boolean> value) {
        scanAllUnmanaged.set(value)
    }

    void setScanAllUnmanaged(Boolean value) {
        scanAllUnmanaged.set(value)
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

    private final Property<String> command = objects.property(String)

    @Input
    @Optional
    @Option(option = "command", description = """
    Indicate which specific Python commands to use based on Python version. The default is python
    which executes your default python version. Run 'python -V' to find out what version it
    is. If you are using multiple Python versions, use this parameter to specify the correct Python
    command for execution.
    Default: python
    Example: --command=python3
    """)
    Property<String> getCommand() {
        command
    }

    void setCommand(Provider<String> value) {
        command.set(value)
    }

    void setCommand(String value) {
        command.set(value)
    }

    private final Property<Boolean> skipUnresolved = objects.property(Boolean)

    @Input
    @Optional
    @Option(option = "skip-unresolved", description = """
    Allow skipping packages that are not found in the environment.
    """)
    Property<Boolean> getSkipUnresolved() {
        skipUnresolved
    }

    void setSkipUnresolved(Provider<Boolean> value) {
        skipUnresolved.set(value)
    }

    void setSkipUnresolved(Boolean value) {
        skipUnresolved.set(value)
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

    @Option(option = "configuration-attributes", description = """
    Select certain values of configuration attributes to install dependencies and perform dependency
    resolution, for example, buildtype:release,usage:java-runtime.
    """)
    void configurationAttributesOption(String attributes) {
        configurationAttributes(attributes.trim().split(","))
    }

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

    @Option(option = "packages-folder", description = "Specify a custom path to the packages folder.")
    packagesFolder(String dir) {
        packagesFolder.set(layout.projectDirectory.dir(dir))
    }

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
