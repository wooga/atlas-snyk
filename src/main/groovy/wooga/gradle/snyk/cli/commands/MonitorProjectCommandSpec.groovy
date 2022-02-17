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

package wooga.gradle.snyk.cli.commands


import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.options.Option
import wooga.gradle.OptionMapper
import wooga.gradle.snyk.cli.BusinessCriticalityOption
import wooga.gradle.snyk.cli.EnvironmentOption
import wooga.gradle.snyk.cli.LifecycleOption
import wooga.gradle.snyk.cli.options.MonitorOption

/**
 * Provides properties for the Monitor command (a superset of the {@code TestCommandSpec})
 */
trait MonitorProjectCommandSpec extends TestProjectCommandSpec implements OptionMapper<MonitorOption> {

    @Override
    String getOption(MonitorOption option) {
        def value = null

        switch (option) {
            case MonitorOption.trustPolicies:
                if (trustPolicies.getOrElse(false)){
                    value = true
                }
                break

            case MonitorOption.projectEnvironment:
                if (projectEnvironment.present && !projectEnvironment.get().isEmpty()){
                    value = projectEnvironment.get()
                }
                break

            case MonitorOption.projectLifecycle:
                if (projectLifecycle.present && !projectLifecycle.get().isEmpty()){
                    value = projectLifecycle.get()
                }
                break

            case MonitorOption.projectBusinessCriticality:
                if (projectBusinessCriticality.present && !projectBusinessCriticality.get().isEmpty()){
                    value = projectBusinessCriticality.get()
                }
                break

            case MonitorOption.projectTags:
                if (projectTags.present && !projectTags.get().isEmpty()){
                    value = projectTags.get()
                }
                break
        }

        if (value != null){
            return option.compose(value)
        }
        null
    }

    private final Property<Boolean> trustPolicies = objects.property(Boolean)

    @Input
    @Optional
    @Option(option = "trust-policies", description = """
    Apply and use ignore rules from the Snyk policies your dependencies; otherwise ignore rules in
    the dependencies are only shown as a suggestion.
    """)
    Property<Boolean> getTrustPolicies() {
        trustPolicies
    }

    void setTrustPolicies(Provider<Boolean> value) {
        trustPolicies.set(value)
    }

    void setTrustPolicies(Boolean value) {
        trustPolicies.set(value)
    }

    private final ListProperty<EnvironmentOption> projectEnvironment = objects.listProperty(EnvironmentOption)

    @Input
    @Optional
    ListProperty<EnvironmentOption> getProjectEnvironment() {
        projectEnvironment
    }

    void setProjectEnvironment(Iterable<Object> environments) {
        projectEnvironment.set(environments.collect({ it as EnvironmentOption }))
    }

    void setProjectEnvironment(Provider<Iterable<EnvironmentOption>> environments) {
        projectEnvironment.set(environments)
    }

    void projectEnvironment(EnvironmentOption environment) {
        projectEnvironment.add(environment)
    }

    void projectEnvironment(EnvironmentOption... environments) {
        projectEnvironment.addAll(environments)
    }

    void projectEnvironment(Iterable<EnvironmentOption> environments) {
        projectEnvironment.addAll(environments)
    }

    void setProjectEnvironment(String environment) {
        projectEnvironment.add(environment as EnvironmentOption)
    }

    @Option(option = "project-environment", description = """
    Set the project environment to one or more values (comma-separated). To clear the project
    environment set --project-environment=. Allowed values: frontend, backend, internal, external,
    mobile, saas, onprem, hosted, distributed
    """)
    void projectEnvironmentOption(String environment) {
        projectEnvironment(environment.trim().split(",").collect {
            EnvironmentOption.valueOf(it)
        })
    }

    private final ListProperty<LifecycleOption> projectLifecycle = objects.listProperty(LifecycleOption)

    @Input
    @Optional
    ListProperty<LifecycleOption> getProjectLifecycle() {
        projectLifecycle
    }

    void setProjectLifecycle(Provider<Iterable<LifecycleOption>> lifecycle) {
        projectLifecycle.set(lifecycle)
    }

    void setProjectLifecycle(Iterable<Object> lifecycle) {
        projectLifecycle.set(lifecycle.collect({ it as LifecycleOption }))
    }

    void setProjectLifecycle(String lifecycle) {
        projectLifecycle.add(lifecycle as LifecycleOption)
    }

    void projectLifecycle(LifecycleOption lifecycle) {
        projectLifecycle.add(lifecycle)
    }

    void projectLifecycle(LifecycleOption... lifecycles) {
        projectLifecycle.addAll(lifecycles)
    }

    void projectLifecycle(Iterable<LifecycleOption> lifecycles) {
        projectLifecycle.addAll(lifecycles)
    }

    @Option(option = "project-lifecycle", description = """
    Set the project lifecycle to one or more values (comma-separated). To clear the project lifecycle
    set --project-lifecycle=. Allowed values: production, development, sandbox
    """)
    void projectLifecycleOption(String environment) {
        projectLifecycle(environment.trim().split(",").collect {
            LifecycleOption.valueOf(it)
        })
    }

    private final ListProperty<BusinessCriticalityOption> projectBusinessCriticality = objects.listProperty(BusinessCriticalityOption)

    @Input
    @Optional
    ListProperty<BusinessCriticalityOption> getProjectBusinessCriticality() {
        projectBusinessCriticality
    }

    void setProjectBusinessCriticality(Provider<Iterable<BusinessCriticalityOption>> criticality) {
        projectBusinessCriticality.set(criticality)
    }

    void setProjectBusinessCriticality(Iterable<Object> criticality) {
        projectBusinessCriticality.set(criticality.collect({ it as BusinessCriticalityOption }))
    }

    void setProjectBusinessCriticality(String criticality) {
        projectBusinessCriticality.add(criticality as BusinessCriticalityOption)
    }

    void projectBusinessCriticality(BusinessCriticalityOption criticality) {
        projectBusinessCriticality.add(criticality)
    }

    void projectBusinessCriticality(BusinessCriticalityOption... criticalities) {
        projectBusinessCriticality.addAll(criticalities)
    }

    void projectBusinessCriticality(Iterable<BusinessCriticalityOption> criticalities) {
        projectBusinessCriticality.addAll(criticalities)
    }

    @Option(option = "project-business-criticality", description = """
    Set the project business criticality to one or more values (comma-separated). To clear the
    project business criticality set --project-business-criticality=. Allowed values: critical, high,
    medium, low
    """)
    void projectBusinessCriticalityOption(String environment) {
        projectBusinessCriticality(environment.trim().split(",").collect {
            BusinessCriticalityOption.valueOf(it)
        })
    }

    private final MapProperty<String, String> projectTags = objects.mapProperty(String, String)

    @Input
    @Optional
    MapProperty<String, String> getProjectTags() {
        projectTags
    }

    void setProjectTags(Map<String, String> tags) {
        projectTags.set(tags)
    }

    void setProjectTags(Provider<Map<String, String>> tags) {
        projectTags.set(tags)
    }

    void projectTags(Map<String, String> tags) {
        projectTags.putAll(tags)
    }

    void projectTags(Provider<Map<String, String>> tags) {
        projectTags.putAll(tags)
    }

    @Option(option = "project-tags", description = """
    Set the project tags to one or more values (comma-separated key value pairs with an "="
    separator), for example, --project-tags=department=finance,team=alpha. To clear the project tags
    set --project-tags=
    """)
    void projectTags(String values) {
        def tags = values.trim().split(',').collectEntries {
            def parts = it.trim().split("=")
            [(parts[0].toString().trim()): parts[1].toString().trim()]
        } as Map<String, String>

        projectTags.set(tags)
    }

    @Option(option = "tags", description = """
    This is an alias for --project-tags.
    """)
    void tags(String values) {
        projectTags(values)
    }
}
