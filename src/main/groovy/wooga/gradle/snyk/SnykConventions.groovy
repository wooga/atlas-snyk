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

import com.wooga.gradle.PropertyLookup

class SnykConventions {

    static final PropertyLookup token = new PropertyLookup(
            "SNYK_TOKEN",
            "snyk.token",
            null
    )

    static final PropertyLookup executableName = new PropertyLookup(
            "SNYK_EXECUTABLE_NAME",
            "snyk.executableName",
            "snyk"
    )

    static final PropertyLookup version = new PropertyLookup(
            "SNYK_VERSION",
            "snyk.version",
            "latest"
    )

    static final PropertyLookup installationDir = new PropertyLookup(
            "SNYK_INSTALLATION_DIR",
            "snyk.installationDir",
            null
    )

    static final PropertyLookup snykPath = new PropertyLookup(
            ["SNYK_PATH", "SNYK_SNYK_PATH",],
            ["snyk.snykPath", "snyk.path"],
            null
    )

    static final PropertyLookup insecure = new PropertyLookup(
            "SNYK_INSECURE",
            "snyk.insecure",
            false
    )

    static final PropertyLookup logFile = new PropertyLookup(
            "SNYK_LOG_FILE",
            "snyk.logFile",
            null
    )

    static final PropertyLookup allProjects = new PropertyLookup(
            "SNYK_ALL_PROJECTS",
            "snyk.allProjects",
            null
    )

    static final PropertyLookup detectionDepth = new PropertyLookup(
            "SNYK_DETECTION_DEPTH",
            "snyk.detectionDepth",
            null
    )

    static final PropertyLookup exclude = new PropertyLookup(
            "SNYK_EXCLUDE",
            "snyk.exclude",
            null
    )

    static final PropertyLookup pruneRepeatedSubDependencies = new PropertyLookup(
            "SNYK_PRUNE_REPEATED_SUB_DEPENDENCIES",
            "snyk.pruneRepeatedSubDependencies",
            null
    )

    static final PropertyLookup printDependencies = new PropertyLookup(
            "SNYK_PRINT_DEPENDENCIES",
            "snyk.printDependencies",
            null
    )

    static final PropertyLookup remoteRepoUrl = new PropertyLookup(
            "SNYK_REMOTE_REPO_URL",
            "snyk.remoteRepoUrl",
            null
    )

    static final PropertyLookup includeDevelopmentDependencies = new PropertyLookup(
            "SNYK_INCLUDE_DEVELOPMENT_DEPENDENCIES",
            "snyk.includeDevelopmentDependencies",
            null
    )

    static final PropertyLookup orgName = new PropertyLookup(
            ["ORG_NAME", "SNYK_ORG_NAME"],
            "snyk.orgName",
            null
    )

    static final PropertyLookup packageFile = new PropertyLookup(
            "SNYK_PACKAGE_FILE",
            "snyk.packageFile",
            null
    )

    static final PropertyLookup packageManager = new PropertyLookup(
            "SNYK_PACKAGE_MANAGER",
            "snyk.packageManager",
            null
    )

    static final PropertyLookup ignorePolicy = new PropertyLookup(
            "SNYK_IGNORE_POLICY",
            "snyk.ignorePolicy",
            null
    )

    static final PropertyLookup showVulnerablePaths = new PropertyLookup(
            "SNYK_SHOW_VULNERABLE_PATHS",
            "snyk.showVulnerablePaths",
            null
    )

    static final PropertyLookup projectName = new PropertyLookup(
            "SNYK_PROJECT_NAME",
            "snyk.projectName",
            null
    )

    static final PropertyLookup targetReference = new PropertyLookup(
            "SNYK_TARGET_REFERENCE",
            "snyk.targetReference",
            null
    )

    static final PropertyLookup policyPath = new PropertyLookup(
            "SNYK_POLICY_PATH",
            "snyk.policyPath",
            null
    )

    static final PropertyLookup printJson = new PropertyLookup(
            "SNYK_PRINT_JSON",
            "snyk.printJson",
            null
    )

    static final PropertyLookup jsonOutputPath = new PropertyLookup(
            "SNYK_JSON_OUTPUT_PATH",
            "snyk.jsonOutputPath",
            null
    )

    static final PropertyLookup printSarif = new PropertyLookup(
            "SNYK_PRINT_SARIF",
            "snyk.printSarif",
            null
    )

    static final PropertyLookup sarifOutputPath = new PropertyLookup(
            "SNYK_SARIF_OUTPUT_PATH",
            "snyk.sarifOutputPath",
            null
    )

    static final PropertyLookup severityThreshold = new PropertyLookup(
            "SNYK_SEVERITY_THRESHOLD",
            "snyk.severityThreshold",
            null
    )

    static final PropertyLookup failOn = new PropertyLookup(
            "SNYK_FAIL_ON",
            "snyk.failOn",
            null
    )

    static final PropertyLookup compilerArguments = new PropertyLookup(
            "SNYK_COMPILER_ARGUMENTS",
            "snyk.compilerArguments",
            null
    )

    static final PropertyLookup trustPolicies = new PropertyLookup(
            "SNYK_TRUST_POLICIES",
            "snyk.trustPolicies",
            null
    )

    static final PropertyLookup projectEnvironment = new PropertyLookup(
            "SNYK_PROJECT_ENVIRONMENT",
            "snyk.projectEnvironment",
            null
    )

    static final PropertyLookup projectLifecycle = new PropertyLookup(
            "SNYK_PROJECT_LIFECYCLE",
            "snyk.projectLifecycle",
            null
    )

    static final PropertyLookup projectBusinessCriticality = new PropertyLookup(
            "SNYK_PROJECT_BUSINESS_CRITICALITY",
            "snyk.projectBusinessCriticality",
            null
    )

    static final PropertyLookup projectTags = new PropertyLookup(
            "SNYK_PROJECT_TAGS",
            "snyk.projectTags",
            null
    )

    static final PropertyLookup assetsProjectName = new PropertyLookup(
            "SNYK_ASSETS_PROJECT_NAME",
            "snyk.assetsProjectName",
            null
    )

    static final PropertyLookup packagesFolder = new PropertyLookup(
            "SNYK_PACKAGES_FOLDER",
            "snyk.packagesFolder",
            null
    )

    static final PropertyLookup projectNamePrefix = new PropertyLookup(
            "SNYK_PROJECT_NAME_PREFIX",
            "snyk.projectNamePrefix",
            null
    )

    static final PropertyLookup strictOutOfSync = new PropertyLookup(
            "SNYK_STRICT_OUT_OF_SYNC",
            "snyk.strictOutOfSync",
            null
    )

    static final PropertyLookup scanAllUnmanaged = new PropertyLookup(
            "SNYK_SCAN_ALL_UNMANAGED",
            "snyk.scanAllUnmanaged",
            null
    )

    static final PropertyLookup reachable = new PropertyLookup(
            "SNYK_REACHABLE",
            "snyk.reachable",
            null
    )

    static final PropertyLookup reachableTimeout = new PropertyLookup(
            "SNYK_REACHABLE_TIMEOUT",
            "snyk.reachableTimeout",
            null
    )

    static final PropertyLookup subProject = new PropertyLookup(
            "SNYK_SUB_PROJECT",
            "snyk.subProject",
            null
    )

    static final PropertyLookup allSubProjects = new PropertyLookup(
            "SNYK_ALL_SUB_PROJECTS",
            "snyk.allSubProjects",
            null
    )

    static final PropertyLookup configurationMatching = new PropertyLookup(
            "SNYK_CONFIGURATION_MATCHING",
            "snyk.configurationMatching",
            null
    )

    static final PropertyLookup configurationAttributes = new PropertyLookup(
            "SNYK_CONFIGURATION_ATTRIBUTES",
            "snyk.configurationAttributes",
            null
    )

    static final PropertyLookup command = new PropertyLookup(
            "SNYK_COMMAND",
            "snyk.command",
            null
    )

    static final PropertyLookup initScript = new PropertyLookup(
            "SNYK_INIT_SCRIPT",
            "snyk.initScript",
            null
    )

    static final PropertyLookup skipUnresolved = new PropertyLookup(
            "SNYK_SKIP_UNRESOLVED",
            "snyk.skipUnresolved",
            null
    )

    static final PropertyLookup yarnWorkspaces = new PropertyLookup(
            "SNYK_YARN_WORKSPACES",
            "snyk.yarnWorkspaces",
            null
    )

    static final PropertyLookup strategies = new PropertyLookup(
            "SNYK_STRATEGIES",
            "snyk.strategies",
            null
    )

    static final PropertyLookup checkTaskName = new PropertyLookup(
            "SNYK_CHECK_TASK_NAME",
            "snyk.checkTaskName",
            "check"
    )

    static final PropertyLookup publishTaskName = new PropertyLookup(
            "SNYK_PUBLISH_TASK_NAME",
            "snyk.publishTaskName",
            "publish"
    )

    static final PropertyLookup jsonReportsEnabled = new PropertyLookup(
            "SNYK_JSON_REPORTS_ENABLED",
            "snyk.jsonReportsEnabled",
            true
    )

    static final PropertyLookup sarifReportsEnabled = new PropertyLookup(
            "SNYK_SARIF_REPORTS_ENABLED",
            "snyk.sarifReportsEnabled",
            false
    )

    static final PropertyLookup reportsDir = new PropertyLookup(
            "SNYK_REPORTS_DIR",
            "snyk.reportsDir",
            "reports"
    )

    static final PropertyLookup debug = new PropertyLookup(
            "SNYK_DEBUG",
            "snyk.debug",
            null
    )

    static final PropertyLookup autoDownload = new PropertyLookup(
            "SNYK_AUTO_DOWNLOAD",
            "snyk.autoDownload",
            false
    )
}
