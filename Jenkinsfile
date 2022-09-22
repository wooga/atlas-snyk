#!groovy
/*
 * Copyright 2018-2020 Wooga GmbH
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
@Library('github.com/wooga/atlas-jenkins-pipeline@1.x') _

withCredentials([string(credentialsId: 'atlas_plugins_sonar_token', variable: 'sonar_token'),
                 string(credentialsId: 'snyk-integration-token', variable: 'ATLAS_SNYK_INTEGRATION_TOKEN'),
                 string(credentialsId: 'snyk-wooga-frontend-integration-token', variable: 'SNYK_TOKEN')
]) {
    buildGradlePlugin platforms: ['macos','windows','linux'],
                      sonarToken: sonar_token
}
