package wooga.gradle.snyk.cli.options

import wooga.gradle.OptionBuilder
import wooga.gradle.OptionSpec
import wooga.gradle.snyk.cli.SnykBooleanOption

enum CommonOption implements OptionSpec {
    debug("-d", new SnykBooleanOption())

    private String flag
    private OptionBuilder builder

    String getFlag() {
        flag
    }

    OptionBuilder getBuilder() {
        builder
    }

    CommonOption(String flag, OptionBuilder builder) {
        this.flag = flag
        this.builder = builder
    }
}
