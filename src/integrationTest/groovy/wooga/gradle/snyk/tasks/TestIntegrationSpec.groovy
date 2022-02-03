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

import com.wooga.gradle.PlatformUtils
import spock.lang.Unroll

import java.nio.file.Files

class TestIntegrationSpec extends SnykCheckBaseIntegrationSpec<Test> {

    @Override
    String getCommandName() {
        "test"
    }

    /**
     * Tests against a NET project
     * https://docs.snyk.io/products/snyk-open-source/language-and-package-manager-support/snyk-for-.net
     */
    @Unroll("runs test on #type NET project")
    def "runs test on NET project"() {

        given: "a NET solution or project"
        buildFile << """
        ${extensionName}.workingDirectory="${workingDir}"
        """.stripIndent()

        if (fileName != null) {
            buildFile << "${subjectUnderTestName}.customFile=${wrapValueBasedOnType(fileName, String)}"
        }

        when:
        def result = runTasksSuccessfully(subjectUnderTestName)

        then:
        outputContains(result, Test.composeStartMessage(workingDir)) || outputContains(result, Test.debugStartMessage)

        where:
        type    | workingDir                                    | fileName
        "nuget" | getCopiedResourceDirectoryPath("net_project") | "net_project.sln"
    }

    /**
     * Tests against Gradle projects
     * https://docs.snyk.io/products/snyk-open-source/language-and-package-manager-support/snyk-for-java-gradle-maven
     */
    @Unroll("runs test on Gradle #name project")
    def "runs test on gradle project"() {

        given: "a gradle project"
        buildFile << """
        ${extensionName}.workingDirectory="${workingDir}"   
        """.stripIndent()

        if (all_projects) {
            buildFile << "\n${subjectUnderTestName}.allProjects=true"
        }

        when:
        def result = runTasksSuccessfully(subjectUnderTestName)

        then:
        outputContains(result, Test.composeStartMessage(workingDir)) || outputContains(result, Test.debugStartMessage)

        where:
        name               | workingDir                                            | all_projects
        "Plugin"           | getCopiedResourceDirectoryPath("gradle_plugin")       | true
        "Skeleton Package" | getCopiedResourceDirectoryPath("gradle_project_skel") | false
    }

    /**
     * @return A file within a 'resources' directory in the project
     */
    static File getResourceFile(String path) {
        def url = TestIntegrationSpec.class.getResource("/" + path)
        new File(url.path)
    }

    /**
     * @return The path to a file within a 'resources' directory in the project
     */
    static String getResourceFilePath(String path) {
        def url = TestIntegrationSpec.class.getResource("/" + path)
        PlatformUtils.escapedPath(osPath(url.path))
    }

    /**
     * @return The path to the copied file/directory (within a temp directory)
     */
    static File copyResourceDirectory(String path) {

        def source = getResourceFile(path)
        if (!source.exists()) {
            return null
        }

        def copy = Files.createTempDirectory(path).toFile();
        org.apache.commons.io.FileUtils.copyDirectory(source, copy)
        copy
    }

    static String getCopiedResourceDirectoryPath(String name) {
        PlatformUtils.escapedPath(osPath(copyResourceDirectory(name).path))
    }
}
