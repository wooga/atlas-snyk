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

import wooga.gradle.snyk.SnykIntegrationSpec
import wooga.gradle.snyk.SnykPlugin

import java.lang.reflect.ParameterizedType

abstract class SnykTaskIntegrationSpec<T extends SnykTask> extends SnykIntegrationSpec {
    Class<T> getSubjectUnderTestClass() {
        if (!_sutClass) {
            try {
                this._sutClass = (Class<T>) ((ParameterizedType) this.getClass().getGenericSuperclass())
                        .getActualTypeArguments()[0];
            }
            catch (Exception e) {
                this._sutClass = (Class<T>) SnykTask
            }
        }
        _sutClass
    }
    private Class<T> _sutClass

    @Override
    String getSubjectUnderTestName() {
        "${subjectUnderTestClass.simpleName.uncapitalize()}Test"
    }

    @Override
    String getSubjectUnderTestTypeName() {
        subjectUnderTestClass.getTypeName()
    }

    def setup() {
        buildFile << """
        ${applyPlugin(SnykPlugin)}
        
        task $subjectUnderTestName(type: ${subjectUnderTestTypeName})
        """.stripIndent()
    }
}
