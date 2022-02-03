/*
 * This Groovy source file was generated by the Gradle 'init' task.
 */
package gradle_plugin

import spock.lang.Specification
import org.gradle.testkit.runner.GradleRunner

/**
 * A simple functional test for the 'gradle_plugin.greeting' plugin.
 */
class Gradle_pluginPluginFunctionalTest extends Specification {
    def "can run task"() {
        given:
        def projectDir = new File("build/functionalTest")
        projectDir.mkdirs()
        new File(projectDir, "settings.gradle") << ""
        new File(projectDir, "build.gradle") << """
            plugins {
                id('gradle_plugin.greeting')
            }
        """

        when:
        def runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withArguments("greeting")
        runner.withProjectDir(projectDir)
        def result = runner.build()

        then:
        result.output.contains("Hello from plugin 'gradle_plugin.greeting'")
    }
}