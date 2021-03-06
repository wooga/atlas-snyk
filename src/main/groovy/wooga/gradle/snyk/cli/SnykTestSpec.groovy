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
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.options.Option

trait SnykTestSpec extends BaseSpec {
    private final Property<Boolean> allProjects = objects.property(Boolean)

    @Input
    @Optional
    @Option(option = "all-projects", description = """
    Auto-detect all projects in the working directory.
    """)
    Property<Boolean> getAllProjects() {
        allProjects
    }

    void setAllProjects(Provider<Boolean> value) {
        allProjects.set(value)
    }

    void setAllProjects(Boolean value) {
        allProjects.set(value)
    }

    private final Property<Integer> detectionDepth = objects.property(Integer)

    @Input
    @Optional
    Property<Integer> getDetectionDepth() {
        detectionDepth
    }

    void setDetectionDepth(Provider<Integer> value) {
        detectionDepth.set(value)
    }

    void setDetectionDepth(Integer value) {
        detectionDepth.set(value)
    }

    @Option(option = "detection-depth", description = """
    Use with options as documented to indicate how many sub-directories to search. DEPTH must be a
    number.
    Default: 4 (the current working directory and 3 sub-directories).
    Example: --detection-depth=3 limits search to the specified directory (or the current directory
    if no <PATH> is specified) plus three levels of subdirectories.
    """)
    void detectionDepth(String value) {
        detectionDepth.set(Integer.parseInt(value))
    }

    private final ListProperty<File> exclude = objects.listProperty(File)

    @Input
    @Optional
    ListProperty<File> getExclude() {
        exclude
    }

    void setExclude(Provider<Iterable<File>> values) {
        exclude.set(values)
    }

    void setExclude(Iterable<File> values) {
        exclude.set(values)
    }

    void exclude(File value) {
        exclude.add(value)
    }

    void exclude(File... values) {
        exclude.addAll(values)
    }

    void exclude(Iterable<File> values) {
        exclude.addAll(values)
    }

    @Option(option = "exclude", description = """
    Can be used with --all-projects and --yarn-workspaces to indicate sub-directories and files to
    exclude. Must be comma separated.
    Use the exclude option with --detection-depth to ignore directories at any depth.
    """)
    void excludeOption(String excludes) {
        exclude.set(excludes.trim().split(',').collect {
            new File(it)
        })
    }

    private final Property<Boolean> pruneRepeatedSubDependencies = objects.property(Boolean)

    @Input
    @Optional
    @Option(option = "prune-repeated-subdependencies", description = """
    Prune dependency trees, removing duplicate sub-dependencies.
    Continues to find all vulnerabilities, but may not find all of the vulnerable paths.
    """)
    Property<Boolean> getPruneRepeatedSubDependencies() {
        pruneRepeatedSubDependencies
    }

    void setPruneRepeatedSubDependencies(Provider<Boolean> value) {
        pruneRepeatedSubDependencies.set(value)
    }

    void setPruneRepeatedSubDependencies(Boolean value) {
        pruneRepeatedSubDependencies.set(value)
    }

    private final Property<Boolean> printDependencies = objects.property(Boolean)

    @Input
    @Optional
    @Option(option = "print-deps", description = """
    Print the dependency tree before sending it for analysis.
    """)
    Property<Boolean> getPrintDependencies() {
        printDependencies
    }

    void setPrintDependencies(Provider<Boolean> value) {
        printDependencies.set(value)
    }

    void setPrintDependencies(Boolean value) {
        printDependencies.set(value)
    }

    private final Property<String> remoteRepoUrl = objects.property(String)

    @Input
    @Optional
    @Option(option = "remote-repo-url", description = """
    Set or override the remote URL for the repository that you would like to monitor.
    """)
    Property<String> getRemoteRepoUrl() {
        remoteRepoUrl
    }

    void setRemoteRepoUrl(Provider<String> value) {
        remoteRepoUrl.set(value)
    }

    void setRemoteRepoUrl(String value) {
        remoteRepoUrl.set(value)
    }

    private final Property<Boolean> includeDevelopmentDependencies = objects.property(Boolean)

    @Input
    @Optional
    @Option(option = "dev", description = """
    Include development-only dependencies. Applicable only for some package managers, for example
    devDependencies in npm or :development dependencies in Gemfile.
    Default: scan only production dependencies.
    """)
    Property<Boolean> getIncludeDevelopmentDependencies() {
        includeDevelopmentDependencies
    }

    void setIncludeDevelopmentDependencies(Provider<Boolean> value) {
        includeDevelopmentDependencies.set(value)
    }

    void setIncludeDevelopmentDependencies(Boolean value) {
        includeDevelopmentDependencies.set(value)
    }

    private final Property<String> orgName = objects.property(String)

    @Input
    @Optional
    @Option(option = "org", description = """
    Specify the <ORG_NAME> to run Snyk commands tied to a specific organization. The <ORG_NAME>
    influences where new projects are created after running the monitor command, some features
    availability, and private test limits.
    If you have multiple organizations, you can set a default from the CLI using:
    \$ snyk config set org=<ORG_NAME>
    Set a default to ensure all newly monitored projects are created under your default organization.
    If you need to override the default, use the --org=<ORG_NAME> option.
    Default: <ORG_NAME> that is the current preferred organization in your Account settings
    https://app.snyk.io/account.
    """)
    Property<String> getOrgName() {
        orgName
    }

    void setOrgName(Provider<String> value) {
        orgName.set(value)
    }

    void setOrgName(String value) {
        orgName.set(value)
    }


    private final RegularFileProperty packageFile = objects.fileProperty()

    @InputFile
    @Optional
    RegularFileProperty getPackageFile() {
        packageFile
    }

    void setPackageFile(Provider<RegularFile> value) {
        packageFile.set(value)
    }

    void setPackageFile(File value) {
        packageFile.set(value)
    }

    @Option(option = "file", description = """
    Specify a package file.
    When testing locally or monitoring a project, you can specify the file that Snyk should inspect
    for package information. When the file is not specified, Snyk tries to detect the appropriate
    file for your project.
    """)
    void packageFile(String value) {
        packageFile.set(layout.projectDirectory.file(value))
    }

    private final Property<String> packageManager = objects.property(String)

    @Input
    @Optional
    @Option(option = "package-manager", description = """
    Specify the name of the package manager when the filename specified with the --file=<FILE> option is not standard.
    This allows Snyk to find the file.
    """)
    Property<String> getPackageManager() {
        packageManager
    }

    void setPackageManager(Provider<String> value) {
        packageManager.set(value)
    }

    void setPackageManager(String value) {
        packageManager.set(value)
    }

    private final Property<Boolean> ignorePolicy = objects.property(Boolean)

    @Input
    @Optional
    @Option(option = "ignore-policy", description = """
    Ignore all set policies, the current policy in the .snyk file, Org level ignores, and the project
    policy on snyk.io.
    """)
    Property<Boolean> getIgnorePolicy() {
        ignorePolicy
    }

    void setIgnorePolicy(Provider<Boolean> value) {
        ignorePolicy.set(value)
    }

    void setIgnorePolicy(Boolean value) {
        ignorePolicy.set(value)
    }

    private final Property<VulnerablePathsOption> showVulnerablePaths = objects.property(VulnerablePathsOption)

    @Input
    @Optional
    @Option(option = "show-vulnerable-paths", description = """
    Display the dependency paths from the top level dependencies down to the vulnerable packages.
    Does not affect output when using JSON --json output.
    Default: some (a few example paths shown).
    false is an alias for none.
    """)
    Property<VulnerablePathsOption> getShowVulnerablePaths() {
        showVulnerablePaths
    }

    void setShowVulnerablePaths(Provider<VulnerablePathsOption> value) {
        showVulnerablePaths.set(value)
    }

    void setShowVulnerablePaths(VulnerablePathsOption value) {
        showVulnerablePaths.set(value)
    }

    void setShowVulnerablePaths(String value) {
        showVulnerablePaths.set(value as VulnerablePathsOption)
    }

    private final Property<String> projectName = objects.property(String)

    @Input
    @Optional
    @Option(option = "project-name", description = """
    Specify a custom Snyk project name.
    """)
    Property<String> getProjectName() {
        projectName
    }

    void setProjectName(Provider<String> value) {
        projectName.set(value)
    }

    void setProjectName(String value) {
        projectName.set(value)
    }

    private final Property<String> targetReference = objects.property(String)

    @Input
    @Optional
    @Option(option = "target-reference", description = """
    Specify a reference which differentiates this project, for example, a branch name or version.
    Projects having the same reference can be grouped based on that reference. Only supported for
    Snyk Open Source. See Separating projects by branch or version https://snyk.info/3B0vTPs.
    """)
    Property<String> getTargetReference() {
        targetReference
    }

    void setTargetReference(Provider<String> value) {
        targetReference.set(value)
    }

    void setTargetReference(String value) {
        targetReference.set(value)
    }

    private final RegularFileProperty policyPath = objects.fileProperty()

    @InputFile
    @Optional
    RegularFileProperty getPolicyPath() {
        policyPath
    }

    void setPolicyPath(Provider<RegularFile> value) {
        policyPath.set(value)
    }

    void setPolicyPath(File value) {
        policyPath.set(value)
    }

    void setPolicyPath(String value) {
        policyPath.set(new File(value))
    }

    @Option(option = "policy-path", description = """
    Manually pass a path to a .snyk policy file.
    """)
    void policyPath(String path) {
        policyPath.set(layout.projectDirectory.file(path))
    }

    private final Property<Boolean> printJson = objects.property(Boolean)

    @Input
    @Optional
    @Option(option = "json", description = """
    Print results in JSON format.
    """)
    Property<Boolean> getPrintJson() {
        printJson
    }

    void setPrintJson(Provider<Boolean> value) {
        printJson.set(value)
    }

    void setPrintJson(Boolean value) {
        printJson.set(value)
    }



    private final Property<Boolean> printSarif = objects.property(Boolean)

    @Input
    @Optional
    @Option(option = "sarif", description = """
    Return results in SARIF format.
    """)
    Property<Boolean> getPrintSarif() {
        printSarif
    }

    void setPrintSarif(Provider<Boolean> value) {
        printSarif.set(value)
    }

    void setPrintSarif(Boolean value) {
        printSarif.set(value)
    }



    private final Property<SeverityThresholdOption> severityThreshold = objects.property(SeverityThresholdOption)

    @Input
    @Optional
    @Option(option = "severity-threshold", description = """
    Report only vulnerabilities at the specified level or higher.
    """)
    Property<SeverityThresholdOption> getSeverityThreshold() {
        severityThreshold
    }

    void setSeverityThreshold(Provider<SeverityThresholdOption> value) {
        severityThreshold.set(value)
    }

    void setSeverityThreshold(SeverityThresholdOption value) {
        severityThreshold.set(value)
    }

    void setSeverityThreshold(String value) {
        severityThreshold.set(value as SeverityThresholdOption)
    }

    private final Property<FailOnOption> failOn = objects.property(FailOnOption)

    @Input
    @Optional
    @Option(option = "fail-on", description = """
    Fail only when there are vulnerabilities that can be fixed.
    -  all: fail when there is at least one vulnerability that can be either upgraded or patched.
    -  upgradable: fail when there is at least one vulnerability that can be upgraded.
    -  patchable: fail when there is at least one vulnerability that can be patched.
    If vulnerabilities do not have a fix and this option is being used, tests pass.
    """)
    Property<FailOnOption> getFailOn() {
        failOn
    }

    void setFailOn(Provider<FailOnOption> value) {
        failOn.set(value)
    }

    void setFailOn(FailOnOption value) {
        failOn.set(value)
    }

    void setFailOn(String value) {
        failOn.set(value as FailOnOption)
    }

    private final ListProperty<String> compilerArguments = objects.listProperty(String)

    @Input
    @Optional
    ListProperty<String> getCompilerArguments() {
        compilerArguments
    }

    void setCompilerArguments(Provider<Iterable<String>> arguments) {
        compilerArguments.set(arguments)
    }

    void setCompilerArguments(Iterable<String> arguments) {
        compilerArguments.set(arguments)
    }

    void compilerArgument(String argument) {
        compilerArguments.add(argument)
    }

    void compilerArguments(String... arguments) {
        compilerArguments.addAll(arguments)
    }

    void compilerArguments(Iterable<String> arguments) {
        compilerArguments.addAll(arguments)
    }
}
