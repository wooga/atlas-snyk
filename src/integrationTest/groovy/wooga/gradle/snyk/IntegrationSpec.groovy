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

import wooga.gradle.snyk.tasks.Snyk

class IntegrationSpec extends com.wooga.gradle.test.IntegrationSpec {

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
            default:
                return rawValue.toString()
        }
    }
}
