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

package wooga.gradle.snyk

import com.wooga.gradle.PlatformUtils
import com.wooga.gradle.PropertyLookup
import com.wooga.gradle.test.IntegrationSpec
import com.wooga.spock.extensions.snyk.Snyk
import org.gradle.api.file.Directory
import spock.lang.Shared
import wooga.gradle.snyk.cli.*

import java.nio.file.Files

import static com.wooga.gradle.PlatformUtils.escapedPath
import static com.wooga.gradle.PlatformUtils.getUnixUserHomePath

class SnykIntegrationSpec extends IntegrationSpec {

    static final String extensionName = SnykPlugin.EXTENSION_NAME

    String getSubjectUnderTestName() {
        "snykIntegrationTest"
    }

    String getSubjectUnderTestTypeName() {
        Snyk.class.name
    }

    void appendToSubjectTask(String... lines) {
        buildFile << """
        $subjectUnderTestName {
            ${lines.join('\n')}
        }
        """.stripIndent()
    }

    @Shared
    List<String> declaredEnvironmentVariables

    def setupSpec() {
        declaredEnvironmentVariables = SnykConventions.declaredFields.findAll({it.type == PropertyLookup}).collect {
            it.setAccessible(true)
            def convention = (PropertyLookup) it.get()
            it.setAccessible(false)
            convention.environmentKeys as List<String>
        }.flatten() as List<String>
    }

    def setup() {
        // Clear out all environment variables which this plugin declares to have a clean test setup.
        // This helps with false positives or true negatives on jenkins or development machines which set
        // some of the environment variables globally
        declaredEnvironmentVariables.each {
            environmentVariables.set(it, null)
        }
    }

    static wrapValueFallback = { Object rawValue, String type, Closure<String> fallback ->
        switch (type) {
            case VulnerablePathsOption.getSimpleName():
                return "${VulnerablePathsOption.canonicalName}.${rawValue.toString()}".toString()
            case EnvironmentOption.getSimpleName():
                return "${EnvironmentOption.canonicalName}.${rawValue.toString()}".toString()
            case LifecycleOption.getSimpleName():
                return "${LifecycleOption.canonicalName}.${rawValue.toString()}".toString()
            case BusinessCriticalityOption.getSimpleName():
                return "${BusinessCriticalityOption.canonicalName}.${rawValue.toString()}".toString()
            case SeverityThresholdOption.getSimpleName():
                return "${SeverityThresholdOption.canonicalName}.${rawValue.toString()}".toString()
            case FailOnOption.getSimpleName():
                return "${FailOnOption.canonicalName}.${rawValue.toString()}".toString()
            case "CLIList":
                def list = rawValue as List
                return list.join(",").toString()
            case "CLIMap":
                def map = rawValue as Map
                return map.collect { key, value ->
                    "${key}=${value}"
                }.join(",").toString()
            default:
                return rawValue.toString()
        }
    }

    static File getGradleUserHome() {
        def gradleUserHomeEnv = System.getenv("GRADLE_USER_HOME")
        if (!gradleUserHomeEnv) {
            gradleUserHomeEnv = "${getUnixUserHomePath()}/.gradle"
        }
        new File(gradleUserHomeEnv)
    }

    void setSnykWrapper() {
        def snykWrapper = generateBatchWrapper("snyk-wrapper")
        def wrapperDir = snykWrapper.parent
        def wrapperPath = escapedPath(wrapperDir)
        buildFile << """
        ${extensionName}.executableName=${wrapValueBasedOnType(snykWrapper.name, String)}
        ${extensionName}.snykPath=${wrapValueBasedOnType(wrapperPath, Directory)}
        """.stripIndent()
    }
}
