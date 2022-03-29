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

import groovy.transform.stc.ClosureParams
import groovy.transform.stc.FromString
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskProvider
import org.gradle.util.ConfigureUtil
import wooga.gradle.snyk.cli.*
import wooga.gradle.snyk.tasks.Monitor
import wooga.gradle.snyk.tasks.Report
import wooga.gradle.snyk.tasks.Test

trait SnykPluginExtension implements
        SnykTestSpec, SnykMonitorSpec, SnykTaskSpec, SnykCommonArgumentSpec, SnykProjectSpec {

    abstract TaskProvider<Test> getSnykTest()

    abstract TaskProvider<Monitor> getSnykMonitor()

    abstract TaskProvider<Report> getSnykReport()

    abstract Project getProject()

    void snykTest(Action<Test> action) {
        snykTest.configure(action)
    }

    void snykTest(@ClosureParams(value = FromString.class, options = ["wooga.gradle.snyk.tasks.Test"]) Closure configure) {
        snykTest.configure(ConfigureUtil.configureUsing(configure))
    }

    void snykMonitor(Action<Monitor> action) {
        snykMonitor.configure(action)
    }

    void snykMonitor(@ClosureParams(value = FromString.class, options = ["wooga.gradle.snyk.tasks.Monitor"]) Closure configure) {
        snykMonitor.configure(ConfigureUtil.configureUsing(configure))
    }

    void snykReport(Action<Report> action) {
        snykReport.configure(action)
    }

    void snykReport(@ClosureParams(value = FromString.class, options = ["wooga.gradle.snyk.tasks.Report"]) Closure configure) {
        snykReport.configure(ConfigureUtil.configureUsing(configure))
    }

    private final Property<Boolean> jsonReportsEnabled = objects.property(Boolean)

    Property<Boolean> getJsonReportsEnabled() {
        jsonReportsEnabled
    }

    void setJsonReportsEnabled(Provider<Boolean> value) {
        jsonReportsEnabled.set(value)
    }

    void setJsonReportsEnabled(Boolean value) {
        jsonReportsEnabled.set(value)
    }

    private final Property<Boolean> htmlReportsEnabled = objects.property(Boolean)

    Property<Boolean> getHtmlReportsEnabled() {
        htmlReportsEnabled
    }

    void setHtmlReportsEnabled(Provider<Boolean> value) {
        htmlReportsEnabled.set(value)
    }

    void setHtmlReportsEnabled(Boolean value) {
        htmlReportsEnabled.set(value)
    }


    private final Property<Boolean> sarifReportsEnabled = objects.property(Boolean)

    Property<Boolean> getSarifReportsEnabled() {
        sarifReportsEnabled
    }

    void setSarifReportsEnabled(Provider<Boolean> value) {
        sarifReportsEnabled.set(value)
    }

    void setSarifReportsEnabled(Boolean value) {
        sarifReportsEnabled.set(value)
    }

    private final DirectoryProperty reportsDir = objects.directoryProperty()

    /**
     * @return The directory where reports generated by Snyk
     */
    @Internal
    DirectoryProperty getReportsDir() {
        reportsDir
    }

    void setReportsDir(Provider<Directory> value) {
        reportsDir.set(value)
    }

    void setReportsDir(String value) {
        def file = new File(value)
        reportsDir.set(file)
    }
}
