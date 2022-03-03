package wooga.gradle.snyk

import org.gradle.api.Project

interface ProjectRegistrationHandler {
    SnykPluginExtension registerProject(File projectFile, SnykRootPluginExtension parentExtension)

    SnykPluginExtension registerProject(Project project, SnykRootPluginExtension parentExtension)
}
