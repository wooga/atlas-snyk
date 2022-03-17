package wooga.gradle.snyk.cli.options

import wooga.gradle.OptionBuilder
import wooga.gradle.OptionSpec
import wooga.gradle.snyk.cli.FailOnOption
import wooga.gradle.snyk.cli.SeverityThresholdOption
import wooga.gradle.snyk.cli.SnykBooleanOption
import wooga.gradle.snyk.cli.SnykEnumOption
import wooga.gradle.snyk.cli.SnykFileOption
import wooga.gradle.snyk.cli.SnykIntegerOption
import wooga.gradle.snyk.cli.SnykListOption
import wooga.gradle.snyk.cli.SnykStringOption
import wooga.gradle.snyk.cli.VulnerablePathsOption

/**
 * Options for the Test command
 * (https://docs.snyk.io/features/snyk-cli/commands/test)
 */
enum TestOption implements OptionSpec {

    allProjects("--all-projects", new SnykBooleanOption()),
    projectName("--project-name", new SnykStringOption()),
    detectionDepth("--detection-depth", new SnykIntegerOption()),
    exclude("--exclude", new SnykListOption(File.class, { File file -> file.path })),
    pruneRepeatedSubDependencies("--prune-repeated-subdependencies", new SnykBooleanOption()),
    printDependencies("--print-deps", new SnykBooleanOption()),
    remoteRepoUrl("--remote-repo-url", new SnykStringOption()),
    includeDevelopmentDependencies("--dev", new SnykBooleanOption()),
    orgName("--org", new SnykStringOption()),
    packageFile("--file", new SnykFileOption()),
    packageManager("--package-manager", new SnykStringOption()),
    ignorePolicy('--ignore-policy', new SnykBooleanOption()),
    showVulnerablePaths("--show-vulnerable-paths", new SnykEnumOption(VulnerablePathsOption.class)),
    targetReference("--target-reference", new SnykStringOption()),
    policyPath("--policy-path", new SnykFileOption()),
    printJson("--json", new SnykBooleanOption()),
    jsonOutputPath("--json-file-output", new SnykFileOption()),
    printSarif("--sarif", new SnykBooleanOption()),
    sarifOutputPath("--sarif-file-output", new SnykFileOption()),
    severityThreshold("--severity-threshold", new SnykEnumOption(SeverityThresholdOption.class)),
    failOn("--fail-on", new SnykEnumOption(FailOnOption.class))

    private String flag
    private OptionBuilder builder

    String getFlag() {
        flag
    }

    OptionBuilder getBuilder() {
        builder
    }

    TestOption(String flag, OptionBuilder builder) {
        this.flag = flag
        this.builder = builder
    }
}
