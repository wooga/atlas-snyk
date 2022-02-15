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

import com.wooga.gradle.ArgumentsSpec
import com.wooga.gradle.io.FileUtils
import com.wooga.gradle.io.LogFileSpec
import com.wooga.gradle.io.OutputStreamSpec
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecResult
import org.gradle.process.ExecSpec
import wooga.gradle.OptionAggregator
import wooga.gradle.snyk.SnykActionSpec
import wooga.gradle.snyk.cli.SnykExecutionException
import wooga.gradle.snyk.cli.SnykTaskSpec

abstract class SnykTask extends DefaultTask
        implements SnykActionSpec, SnykTaskSpec,
                ArgumentsSpec,
                LogFileSpec,
                OutputStreamSpec,
                OptionAggregator {

    @Internal
    ProviderFactory getProviderFactory() {
        getProviders()
    }

    abstract void addMainOptions(List<String> args)

    SnykTask() {
        internalArguments = project.provider({ composeArguments() })
        outputs.upToDateWhen { false }
    }

    @TaskAction
    protected void exec() {

        def _arguments = arguments.get()
        def _workingDir = workingDirectory.getOrNull()
        def _executable
        if (snykPath.present) {
            _executable = snykPath.file(executableName).get().asFile.path
        } else {
            _executable = executableName.get()
        }

        if (logFile.present){
            FileUtils.ensureFile(logFile)
        }

        ExecResult execResult = project.exec(new Action<ExecSpec>() {
            @Override
            void execute(ExecSpec exec) {
                exec.with {
                    executable _executable
                    args = _arguments
                    ignoreExitValue = true
                    standardOutput = getOutputStream(logFile.get().asFile)
                    errorOutput = getOutputStream(logFile.get().asFile)

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

    static void handleExitCode(int exitValue) {
        /*
            Possible exit codes and their meaning:
                0: success, no vulnerabilities found
                1: action_needed, vulnerabilities found
                2: failure, try to re-run command
                3: failure, no supported projects detected\
         */
        if (exitValue != 0) {
            throw new SnykExecutionException(exitValue)
        }
    }
}


