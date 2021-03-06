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

import nebula.test.ProjectSpec
import spock.lang.Unroll
import wooga.gradle.snyk.tasks.*

class SnykPluginSpec extends ProjectSpec {

    public static final String PLUGIN_NAME = 'net.wooga.snyk'

    @Unroll("creates the task #taskName")
    def 'Creates needed tasks'(String taskName, Class taskType) {
        given:
        assert !project.plugins.hasPlugin(PLUGIN_NAME)
        assert !project.tasks.findByName(taskName)

        when:
        project.plugins.apply(PLUGIN_NAME)

        then:
        def task = project.tasks.findByName(taskName)
        taskType.isInstance(task)

        where:
        taskName            | taskType
        "snykTest"          | Test
        "snykMonitor"       | Monitor
        "snykReport"        | Report
        "snykInstall"       | SnykInstall
        "snykToHtmlInstall" | SnykToHtmlInstall
    }

    @Unroll
    def "Creates the '#extensionName' extension with type #extensionType"() {
        given:
        assert !project.plugins.hasPlugin(PLUGIN_NAME)
        assert !project.extensions.findByName(extensionName)

        when:
        project.plugins.apply(PLUGIN_NAME)

        then:
        def extension = project.extensions.findByName(extensionName)
        extensionType.isInstance extension

        where:
        extensionName | extensionType
        'snyk'        | SnykRootPluginExtension
        'snykToHtml'  | SnykToHtmlPluginExtension
    }

    @Unroll
    def "Creates the '#extensionName' extension with type #extensionType on registered subproject"() {
        given: "a project with snyk plugin added"
        project.plugins.apply(PLUGIN_NAME)

        and: "a sub project"
        def subProject = addSubproject("sub1")

        and: "task does not yet exist"
        assert !subProject.extensions.findByName(extensionName)

        and:
        SnykRootPluginExtension rootExtension = project.extensions.findByName(extensionName) as SnykRootPluginExtension

        when:
        rootExtension.registerProject(subProject)

        then:
        def extension = subProject.extensions.findByName(extensionName)
        extensionType.isInstance extension
        !notOfType.isInstance(extension)

        where:
        extensionName | extensionType       | notOfType
        'snyk'        | SnykPluginExtension | SnykRootPluginExtension
    }

    @Unroll
    def "Creates task #taskName on registered subproject"() {
        given: "a project with snyk plugin added"
        project.plugins.apply(PLUGIN_NAME)

        and: "a sub project"
        def subProject = addSubproject("sub1")

        and: "task does not yet exist"
        assert !subProject.tasks.findByName(taskName)

        and:
        SnykRootPluginExtension extension = project.extensions.findByName("snyk") as SnykRootPluginExtension

        when:
        extension.registerProject(subProject)

        then:
        subProject.extensions.findByName("snyk")
        def task = project.tasks.findByName(taskName)
        taskType.isInstance(task)

        where:
        taskName      | taskType
        "snykTest"    | Test
        "snykMonitor" | Monitor
        "snykReport"  | Report
    }

    @Unroll
    def "Registers multiple subprojects with #registerType"() {
        given: "a project with snyk plugin added"
        project.plugins.apply(PLUGIN_NAME)

        and: "multiple sub projects"
        def subProject1 = addSubproject("sub1")
        def subProject2 = addSubproject("sub2")

        and: "task does not yet exist"
        assert !subProject1.extensions.findByName(extensionName)
        assert !subProject2.extensions.findByName(extensionName)

        and:
        SnykRootPluginExtension rootExtension = project.extensions.findByName(extensionName) as SnykRootPluginExtension

        when:
        if (registerType == "Iterable<Project>") {
            rootExtension.registerProject([subProject1, subProject2])
        } else if (registerType == "registerAllSubProjects") {
            rootExtension.registerAllSubProjects()
        }

        then:
        def extension1 = subProject1.extensions.findByName(extensionName)
        extensionType.isInstance extension1
        !notOfType.isInstance(extension1)

        def extension2 = subProject2.extensions.findByName(extensionName)
        extensionType.isInstance extension2
        !notOfType.isInstance(extension2)

        where:
        extensionName | extensionType       | notOfType               | registerType
        'snyk'        | SnykPluginExtension | SnykRootPluginExtension | "Iterable<Project>"
        'snyk'        | SnykPluginExtension | SnykRootPluginExtension | "registerAllSubProjects"
    }

    @Unroll
    def "Snyk task #taskName from registered project #message wired to root snykInstall task if autoDownload property is #autoDownload"() {
        given: "a project with snyk plugin added"
        project.plugins.apply(PLUGIN_NAME)

        and: "multiple sub projects"
        def subProject1 = addSubproject("sub1")

        and: "task does not yet exist"
        assert !subProject1.extensions.findByName('snyk')

        and:
        SnykRootPluginExtension rootExtension = project.extensions.findByName('snyk') as SnykRootPluginExtension
        rootExtension.autoDownload.set(autoDownload)

        when:
        rootExtension.registerProject(subProject1)

        then:
        def snykInstall = project.tasks.named('snykInstall')
        subProject1.tasks.findByName(taskName).dependsOn.contains(snykInstall) == autoDownload

        where:
        taskName      | autoDownload
        'snykMonitor' | true
        'snykTest'    | true
        'snykReport'  | true
        'snykMonitor' | false
        'snykTest'    | false
        'snykReport'  | false
        message = autoDownload ? "will be" : "will not be"
    }

    @Unroll
    def "Snyk task #taskName from registered project #message wired to root snykToHtmlInstall task if autoDownload property is #autoDownload and html reports are #htmlReportEnabled"() {
        given: "a project with snyk plugin added"
        project.plugins.apply(PLUGIN_NAME)

        and: "multiple sub projects"
        def subProject1 = addSubproject("sub1")

        and: "task does not yet exist"
        assert !subProject1.extensions.findByName('snyk')

        and:
        SnykRootPluginExtension rootExtension = project.extensions.findByName('snyk') as SnykRootPluginExtension
        rootExtension.autoDownload.set(autoDownload)
        rootExtension.htmlReportsEnabled.set(htmlReportEnabled)

        when:
        rootExtension.registerProject(subProject1)

        then:
        def snykInstall = project.tasks.named('snykToHtmlInstall')
        subProject1.tasks.findByName(taskName).dependsOn.contains(snykInstall) == expectedResult

        where:
        taskName     | autoDownload | htmlReportEnabled || expectedResult
        'snykTest'   | true         | true              || true
        'snykReport' | true         | true              || true
        'snykTest'   | false        | true              || false
        'snykReport' | false        | true              || false
        'snykTest'   | false        | false             || false
        'snykReport' | false        | false             || false
        'snykTest'   | true         | false             || false
        'snykReport' | true         | false             || true
        message = expectedResult ? "will be" : "will not be"
    }

    @Unroll
    def "Creates the '#extensionName' extension with type #extensionType for registered project #projectType #projectFile"() {
        given: "a project with snyk plugin added"
        project.plugins.apply(PLUGIN_NAME)

        and: "a project file"
        def _projectFile = new File(projectDir, projectFile)
        if (projectType == "file") {
            _projectFile.parentFile.mkdirs()
            _projectFile.text = "Something"
        } else {
            _projectFile.mkdirs()
        }

        and: "task does not yet exist"
        assert !project.extensions.findByName(extensionName)

        and:
        SnykRootPluginExtension rootExtension = project.extensions.findByName("snyk") as SnykRootPluginExtension

        when:
        rootExtension.registerProject(_projectFile)

        then:
        def extension = project.extensions.findByName(extensionName)
        extensionType.isInstance extension
        !notOfType.isInstance(extension)

        where:
        projectFile           | extensionName              | projectType | extensionType       | notOfType
        "test.properties"     | 'snyk.test.properties'     | "file"      | SnykPluginExtension | SnykRootPluginExtension
        "foo/test.properties" | 'snyk.foo.test.properties' | "file"      | SnykPluginExtension | SnykRootPluginExtension
        "test"                | 'snyk.test'                | "dir"       | SnykPluginExtension | SnykRootPluginExtension
        "foo/test"            | 'snyk.foo.test'            | "dir"       | SnykPluginExtension | SnykRootPluginExtension
    }

    @Unroll
    def "Snyk task #taskName from registered project file #message wired to snykInstall task if autoDownload property is #autoDownload"() {
        given: "a project with snyk plugin added"
        project.plugins.apply(PLUGIN_NAME)

        and: "a project file"
        def _projectFile = new File(projectDir, projectFile)
        _projectFile.parentFile.mkdirs()
        _projectFile.text = "Something"

        and:
        SnykRootPluginExtension rootExtension = project.extensions.findByName('snyk') as SnykRootPluginExtension
        rootExtension.autoDownload.set(autoDownload)

        when:
        rootExtension.registerProject(_projectFile)

        then:
        def snykInstall = project.tasks.named('snykInstall')
        project.tasks.findByName(taskName).dependsOn.contains(snykInstall) == autoDownload

        where:
        taskName                      | autoDownload
        'snykMonitor.test.properties' | true
        'snykTest.test.properties'    | true
        'snykReport.test.properties'  | true
        'snykMonitor.test.properties' | false
        'snykTest.test.properties'    | false
        'snykReport.test.properties'  | false
        projectFile = 'test.properties'
        message = autoDownload ? "will be" : "will not be"
    }

    @Unroll
    def "Snyk task #taskName from registered project file #message wired to root snykToHtmlInstall task if autoDownload property is #autoDownload and html reports are #htmlReportEnabled"() {
        given: "a project with snyk plugin added"
        project.plugins.apply(PLUGIN_NAME)

        and: "a project file"
        def _projectFile = new File(projectDir, projectFile)
        _projectFile.parentFile.mkdirs()
        _projectFile.text = "Something"

        and:
        SnykRootPluginExtension rootExtension = project.extensions.findByName('snyk') as SnykRootPluginExtension
        rootExtension.autoDownload.set(autoDownload)
        rootExtension.htmlReportsEnabled.set(htmlReportEnabled)

        when:
        rootExtension.registerProject(_projectFile)

        then:
        def snykInstall = project.tasks.named('snykToHtmlInstall')
        project.tasks.findByName(taskName).dependsOn.contains(snykInstall) == expectedResult

        where:
        taskName     | autoDownload | htmlReportEnabled || expectedResult
        'snykTest'   | true         | true              || true
        'snykReport' | true         | true              || true
        'snykTest'   | false        | true              || false
        'snykReport' | false        | true              || false
        'snykTest'   | false        | false             || false
        'snykReport' | false        | false             || false
        'snykTest'   | true         | false             || false
        'snykReport' | true         | false             || true                 // report will alwasy set htmlReportEnabled to true

        projectFile = 'test.properties'
        message = expectedResult ? "will be" : "will not be"
    }

    @Unroll
    def "throws #exceptionType when registered project #projectType does not exist"() {
        given: "a project with snyk plugin added"
        project.plugins.apply(PLUGIN_NAME)

        and:
        SnykRootPluginExtension rootExtension = project.extensions.findByName("snyk") as SnykRootPluginExtension

        when:
        rootExtension.registerProject(new File(projectDir, projectFile))

        then:
        thrown(exceptionType)

        where:
        projectFile       | projectType
        "test.properties" | "file"
        "foo"             | "dir"
        exceptionType = FileNotFoundException
    }

    @Unroll
    def "Creates task #taskName with type #taskType for registered project #projectType #projectFile"() {
        given: "a project with snyk plugin added"
        project.plugins.apply(PLUGIN_NAME)

        and: "a project file"
        def _projectFile = new File(projectDir, projectFile)
        if (projectType == "file") {
            _projectFile.parentFile.mkdirs()
            _projectFile.text = "Something"
        } else {
            _projectFile.mkdirs()
        }

        and: "task does not yet exist"
        assert !project.tasks.findByName(taskName)

        and:
        SnykRootPluginExtension rootExtension = project.extensions.findByName("snyk") as SnykRootPluginExtension

        when:
        rootExtension.registerProject(_projectFile)

        then:
        def task = project.tasks.findByName(taskName)
        taskType.isInstance(task)

        where:
        projectFile           | taskName                          | projectType | taskType
        "test.properties"     | "snykTest.test.properties"        | "file"      | Test
        "test"                | "snykTest.test"                   | "dir"       | Test
        "foo/test.properties" | "snykTest.foo.test.properties"    | "file"      | Test
        "bar/test"            | "snykTest.bar.test"               | "dir"       | Test
        "test.properties"     | "snykMonitor.test.properties"     | "file"      | Monitor
        "test"                | "snykMonitor.test"                | "dir"       | Monitor
        "baz/test.properties" | "snykMonitor.baz.test.properties" | "file"      | Monitor
        "foo/test"            | "snykMonitor.foo.test"            | "dir"       | Monitor
        "test.properties"     | "snykReport.test.properties"      | "file"      | Report
        "test"                | "snykReport.test"                 | "dir"       | Report
        "bar/test.properties" | "snykReport.bar.test.properties"  | "file"      | Report
        "baz/test"            | "snykReport.baz.test"             | "dir"       | Report
    }
}
