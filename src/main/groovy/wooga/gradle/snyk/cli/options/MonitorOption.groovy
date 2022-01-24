package wooga.gradle.snyk.cli.options

import wooga.gradle.OptionBuilder
import wooga.gradle.OptionSpec
import wooga.gradle.snyk.cli.BusinessCriticalityOption
import wooga.gradle.snyk.cli.EnvironmentOption
import wooga.gradle.snyk.cli.LifecycleOption
import wooga.gradle.snyk.cli.SnykBooleanOption
import wooga.gradle.snyk.cli.SnykEnumListOption
import wooga.gradle.snyk.cli.SnykMapOption

/**
 * Options specific to the Monitor command, which is a superset of the Test command
 * (https://docs.snyk.io/features/snyk-cli/commands/monitor)
 */
enum MonitorOption implements OptionSpec {

    trustPolicies("--trust-policies", new SnykBooleanOption()),
    projectEnvironment("--project-environment", new SnykEnumListOption(EnvironmentOption.class)),
    projectLifecycle("--project-lifecycle", new SnykEnumListOption(LifecycleOption.class)),
    projectBusinessCriticality("--project-business-criticality", new SnykEnumListOption(BusinessCriticalityOption.class)),
    projectTags("--project-tags", new SnykMapOption())

    private String flag
    private OptionBuilder builder

    String getFlag() {
        flag
    }

    OptionBuilder getBuilder() {
        builder
    }

    MonitorOption(String flag, OptionBuilder builder) {
        this.flag = flag
        this.builder = builder
    }
}
