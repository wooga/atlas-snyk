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

package wooga.gradle.snyk.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import wooga.gradle.snyk.SnykActionSpec
import wooga.gradle.snyk.cli.SnykTaskSpec

abstract class SnykTask extends DefaultTask implements SnykActionSpec, SnykTaskSpec  {

    private final RegularFileProperty logFile = project.objects.fileProperty()

    @OutputFile
    RegularFileProperty getLogFile() {
        logFile
    }

    void setLogFile(Provider<RegularFile> value) {
        logFile.set(value)
    }

    void setLogFile(File value) {
        logFile.set(value)
    }

    private final ListProperty<String> additionalArguments = project.objects.listProperty(String)

    @Override
    void setAdditionalArguments(Iterable<String> value) {
        additionalArguments.set(value)
    }

    @Override
    void setAdditionalArguments(Provider<? extends Iterable<String>> value) {
        additionalArguments.set(value)
    }

    @Override
    void argument(String argument) {
        additionalArguments.add(argument)
    }

    @Override
    void arguments(String... arguments) {
        additionalArguments.addAll(arguments)
    }

    @Override
    void arguments(Iterable<String> arguments) {
        additionalArguments.addAll(arguments)
    }

    @TaskAction
    protected void exec() {
        def arguments = arguments.get()
    }
}
