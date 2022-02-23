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
import com.wooga.spock.extensions.snyk.Snyk
import org.apache.commons.io.FileUtils
import spock.lang.Shared
import spock.lang.Unroll

import java.nio.file.Files

abstract class SnykTestBaseIntegrationSpec<T extends SnykTask> extends SnykCheckBaseIntegrationSpec<T> {

    @Shared
    @Snyk
    File snykExecutable

    def setDownloadedSnyk() {
        environmentVariables.set("SNYK_PATH", snykExecutable.parentFile.path)

        appendToSubjectTask("""
        snykPath = file(System.getenv('SNYK_PATH'))
        executableName = "${snykExecutable.name}"
        """.stripIndent())
    }

    def setSnykToken(String token = null) {
        token = token ?: System.getenv("ATLAS_SNYK_INTEGRATION_TOKEN")
        buildFile << """
            ${subjectUnderTestName}.token = "${token}"
        """.stripIndent()
    }

    def setSnykPolicy(File policy = null) {
        appendToSubjectTask("""
        policyPath = ${wrapValueBasedOnType(policy ?: snykPolicy, File)}
        """.stripIndent())
    }

    File snykPolicy

    def setup() {
        snykPolicy = createFile(".snyk", projectDir)
    }

    @Unroll
    def "generates #reportType report"() {
        given: "a NET solution or project"
        setDownloadedSnyk()
        setSnykToken()

        and: "an empty policy file (see: https://github.com/snyk/policy/issues/61)"
        setSnykPolicy()

        and: "a mocked project"
        copyToProject(getResourceFile("net_project"), projectDir)

        and: "project-specific configurations for it"
        appendToSubjectTask("""
            packageFile=${wrapValueBasedOnType("net_project.sln", File)} 
            """.stripIndent())

        and: "a future report file"
        def jsonReport = new File(projectDir, jsonReportLocation)
        assert !jsonReport.exists()
        def sarifReport = new File(projectDir, sarifReportLocation)
        assert !sarifReport.exists()

        if (jsonEnabled) {
            appendToSubjectTask("""
            reports.json.enabled=true
            reports.json.outputLocation=${wrapValueBasedOnType(jsonReportLocation, File)}  
            """.stripIndent())
        }

        if (sarifEnabled) {
            appendToSubjectTask("""
            reports.sarif.enabled=true
            reports.sarif.outputLocation=${wrapValueBasedOnType(sarifReportLocation, File)}  
            """.stripIndent())
        }

        when:
        def result = runTasks(subjectUnderTestName)

        then:
        result.success
        result.wasExecuted(subjectUnderTestName)
        !result.wasSkipped(subjectUnderTestName)
        jsonReport.exists() == jsonEnabled
        sarifReport.exists() == sarifEnabled

        where:
        reportType   | jsonEnabled | sarifEnabled
        "json"       | true        | false
        "sarif"      | false       | true
        "json&sarif" | true        | true
        jsonReportLocation = "build/reports/report.json"
        sarifReportLocation = "build/reports/report.sarif"
    }

    /**
     * Tests against a NET project
     * https://docs.snyk.io/products/snyk-open-source/language-and-package-manager-support/snyk-for-.net
     */
    @Unroll("runs on #type NET project")
    def "runs test on NET project"() {
        given: "a NET solution or project"
        setDownloadedSnyk()
        setSnykToken()
        copyToProject(projectToCopy, projectDir)

        and: "project-specific configurations for it"
        if (fileName != null) {
            buildFile << """
            ${subjectUnderTestName}.packageFile=${wrapValueBasedOnType(fileName, File)}
            """.stripIndent()
        }

        and: "an empty policy file (see: https://github.com/snyk/policy/issues/61)"
        setSnykPolicy()

        and: "set log output enabled to stdout"
        appendToSubjectTask("""
        logToStdout = true
        """.stripIndent())

        when:
        def result = runTasks(subjectUnderTestName)

        then:
        outputContains(result, composeStartMessage(projectDir.path)) || outputContains(result, debugStartMessage)

        where:
        type    | projectToCopy                  | fileName
        "nuget" | getResourceFile("net_project") | "net_project.sln"
    }

    /**
     * Tests against Gradle projects
     * https://docs.snyk.io/products/snyk-open-source/language-and-package-manager-support/snyk-for-java-gradle-maven
     */
    @Unroll("runs on Gradle #name project")
    def "runs test on gradle project"() {

        given: "a gradle project"
        setDownloadedSnyk()
        setSnykToken()
        copyToProject(getResourceFile(projectToCopy), projectDir, false)
        addSubproject(projectToCopy)

        and: "project-specific configurations for it"
        buildFile << "\n${subjectUnderTestName}.allSubProjects=true"

        and: "set log output enabled to stdout"
        appendToSubjectTask("""
        logToStdout = true
        """.stripIndent())

        when:
        def result = runTasks(subjectUnderTestName)

        then:
        outputContains(result, composeStartMessage(projectDir.path)) || outputContains(result, debugStartMessage)

        where:
        name     | projectToCopy
        "Plugin" | "gradle_plugin"
    }

    /**
     * @return A file within a 'resources' directory in the project
     */
    static File getResourceFile(String path) {
        def url = SnykTestBaseIntegrationSpec.class.getResource("/" + path)
        new File(url.path)
    }

    /**
     * @return The path to a file within a 'resources' directory in the project
     */
    static String getResourceFilePath(String path) {
        def url = SnykTestBaseIntegrationSpec.class.getResource("/" + path)
        PlatformUtils.escapedPath(osPath(url.path))
    }

    /**
     * Copies the file/directory onto the gradle project
     */
    static File copyToProject(File file, File projectDir, Boolean merge = true) {
        if (file.directory) {
            if (merge) {
                FileUtils.copyDirectory(file, projectDir)
            } else {
                def subDir = new File(projectDir, file.name)
                FileUtils.copyDirectory(file, subDir)
            }
        } else {
            FileUtils.copyToDirectory(file, projectDir)
        }
    }

    /**
     * @return The path to the copied file/directory (within a temp directory)
     */
    static File copyResourceDirectoryToTemp(String path) {

        def source = getResourceFile(path)
        if (!source.exists()) {
            return null
        }

        def copy = Files.createTempDirectory(path).toFile();
        copy.deleteOnExit()
        FileUtils.copyDirectory(source, copy)
        copy
    }

    static String getCopiedToTempResourceDirectoryPath(String name) {
        PlatformUtils.escapedPath(osPath(copyResourceDirectoryToTemp(name).path))
    }

    static String composeStartMessage(String workingDir) {
        "Testing ${workingDir.replace("\\\\", '\\')}"
    }

    static final String debugStartMessage = "===== DEBUG INFORMATION START ====="
}
