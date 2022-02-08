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

import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecResult
import org.gradle.process.ExecSpec
import wooga.gradle.OptionAggregator
import wooga.gradle.snyk.SnykActionSpec
import wooga.gradle.snyk.cli.SnykExitCode
import wooga.gradle.snyk.cli.SnykTaskSpec
import wooga.gradle.snyk.internal.ArgumentsSpec

abstract class SnykTask extends DefaultTask
        implements SnykActionSpec, SnykTaskSpec, ArgumentsSpec, OptionAggregator {

    @Internal
    ProviderFactory getProviderFactory() {
        getProviders()
    }

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

    abstract void addMainOptions(List<String> args)

    SnykTask() {
        arguments.set(project.provider({ composeArguments() }))
    }

    @TaskAction
    protected void exec() {

        def _arguments = getAllArguments()
        def _workingDir = workingDirectory.getOrNull()
        def _executable
        if (snykPath.present) {
            _executable = snykPath.file(executableName).get().asFile.path
        } else {
            _executable = executableName.get()
        }

        ExecResult execResult = project.exec(new Action<ExecSpec>() {
            @Override
            void execute(ExecSpec exec) {
                exec.with {
                    executable _executable
                    args = _arguments
                    ignoreExitValue = true
                    // Optional working directory
                    if (_workingDir != null) {
                        workingDir(_workingDir)
                    }
                }
            }
        })
        handleExitCode(execResult.exitValue)
    }

    List<String> composeArguments() {
        List<String> args = new ArrayList<String>()
        addMainOptions(args)
        args
    }

    protected void handleExitCode(int code) {
        /*
            Possible exit codes and their meaning:
                0: success, no vulnerabilities found
                1: action_needed, vulnerabilities found
                2: failure, try to re-run command
                3: failure, no supported projects detected\
         */
        SnykExitCode testExitCode = SnykExitCode.values()[code]
        logger.info(testExitCode.message)
    }
}
