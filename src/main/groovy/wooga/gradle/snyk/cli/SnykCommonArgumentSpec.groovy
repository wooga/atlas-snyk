package wooga.gradle.snyk.cli

import com.wooga.gradle.BaseSpec
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.options.Option

trait SnykCommonArgumentSpec extends BaseSpec {
    private final Property<Boolean> insecure = objects.property(Boolean)

    @Input
    @Optional
    @Option(option = "insecure", description = """
    Ignore unknown certificate authorities
    """)
    Property<Boolean> getInsecure() {
        insecure
    }

    void setInsecure(Provider<Boolean> value) {
        insecure.set(value)
    }

    void setInsecure(Boolean value) {
        insecure.set(value)
    }

    private final Property<Boolean> debug = objects.property(Boolean)

    @Input
    @Optional
    Property<Boolean> getDebug() {
        debug
    }

    void setDebug(Provider<Boolean> value) {
        debug.set(value)
    }

    void setDebug(Boolean value) {
        debug.set(value)
    }
}
