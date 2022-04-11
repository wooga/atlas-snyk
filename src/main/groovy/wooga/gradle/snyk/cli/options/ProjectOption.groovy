package wooga.gradle.snyk.cli.options

import wooga.gradle.OptionBuilder
import wooga.gradle.OptionSpec
import wooga.gradle.snyk.cli.SnykBooleanOption
import wooga.gradle.snyk.cli.SnykFileOption
import wooga.gradle.snyk.cli.SnykIntegerOption
import wooga.gradle.snyk.cli.SnykListOption
import wooga.gradle.snyk.cli.SnykStringOption

/**
 * Options specific to languages / project types.
 * (Used by the Test, Monitor commands)
 */
enum ProjectOption implements OptionSpec {

    // Maven
    scanAllUnmanaged("--scan-all-unmanaged", new SnykBooleanOption()),

    // Gradle
    subProject("--sub-project", new SnykStringOption()),
    allSubProjects("--all-sub-projects", new SnykBooleanOption()),
    configurationMatching("--configuration-matching", new SnykStringOption()),
    configurationAttributes("--configuration-attributes", new SnykListOption(String.class)),
    initScript("--init-script", new SnykFileOption()),

    // Maven, Gradle
    reachable("--reachable", new SnykBooleanOption()),
    reachableTimeout("--reachable-timeout", new SnykIntegerOption()),

    // NuGet
    assetsProjectName("--assets-project-name", new SnykBooleanOption()),
    packagesFolder("--packages-folder", new SnykFileOption()),
    projectNamePrefix("--project-name-prefix", new SnykStringOption()),

    // npm, Yarn, CocoaPods
    strictOutOfSync("--strict-out-of-sync", new SnykBooleanOption()),

    // Yarn
    yarnWorkspaces("--yarn-workspaces", new SnykBooleanOption()),

    // Python
    skipUnresolved("--skip-unresolved", new SnykBooleanOption()),
    command("--command", new SnykStringOption())

    private String flag
    private OptionBuilder builder

    String getFlag() {
        flag
    }

    OptionBuilder getBuilder() {
        builder
    }

    ProjectOption(String flag, OptionBuilder builder) {
        this.flag = flag
        this.builder = builder
    }
}
