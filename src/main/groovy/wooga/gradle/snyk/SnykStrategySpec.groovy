package wooga.gradle.snyk

import com.wooga.gradle.BaseSpec
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider

trait SnykStrategySpec extends BaseSpec {

    private final ListProperty<String> strategies = objects.listProperty(String)

    ListProperty<String> getStrategies() {
        strategies
    }

    void setStrategies(Provider<Iterable<String>> values) {
        strategies.set(values)
    }

    void setStrategies(Iterable<String> values) {
        strategies.set(values)
    }

    void strategy(String strategy) {
        strategies.add(strategy)
    }

    void strategy(Provider<String> strategy) {
        strategies.add(strategy)
    }

    void strategies(String... values) {
        strategies.addAll(values)
    }

    void strategies(Iterable<String> values) {
        strategies.addAll(values)
    }

    private final Property<String> checkTaskName = objects.property(String)

    Property<String> getCheckTaskName() {
        checkTaskName
    }

    void setCheckTaskName(Provider<String> value) {
        checkTaskName.set(value)
    }

    void setCheckTaskName(String value) {
        checkTaskName.set(value)
    }

    private final Property<String> publishTaskName = objects.property(String)

    Property<String> getPublishTaskName() {
        publishTaskName
    }

    void setPublishTaskName(Provider<String> value) {
        publishTaskName.set(value)
    }

    void setPublishTaskName(String value) {
        publishTaskName.set(value)
    }

}
