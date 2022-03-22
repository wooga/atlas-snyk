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

import wooga.gradle.OptionMapper
import wooga.gradle.snyk.cli.SnykMonitorSpec
import wooga.gradle.snyk.cli.options.MonitorOption

/**
 * Provides properties for the Monitor command (a superset of the {@code TestCommandSpec})
 */
trait MonitorProjectCommandSpec implements SnykMonitorSpec, OptionMapper<MonitorOption> {

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


}
