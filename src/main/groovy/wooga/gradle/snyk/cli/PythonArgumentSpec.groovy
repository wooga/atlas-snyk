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

package wooga.gradle.snyk.cli

import com.wooga.gradle.BaseSpec
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.options.Option

trait PythonArgumentSpec extends BaseSpec {
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
}
