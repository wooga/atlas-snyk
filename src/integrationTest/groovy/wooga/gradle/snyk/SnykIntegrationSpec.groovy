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

import com.wooga.gradle.test.IntegrationSpec
import wooga.gradle.snyk.cli.*

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
}
