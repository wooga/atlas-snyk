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

package wooga.gradle.snyk.cli

import com.wooga.gradle.BaseSpec
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.options.Option

trait MavenArgumentsSpec extends BaseSpec {
    private final Property<Boolean> scanAllUnmanaged = objects.property(Boolean)

    @Input
    @Optional
    @Option(option = "scan-all-unmanaged", description = """
    Auto-detect maven jars, aars, and wars in given directory. To test individually use
    --file=<JAR_FILE_NAME>.
    """)
    Property<Boolean> getScanAllUnmanaged() {
        scanAllUnmanaged
    }

    void setScanAllUnmanaged(Provider<Boolean> value) {
        scanAllUnmanaged.set(value)
    }

    void setScanAllUnmanaged(Boolean value) {
        scanAllUnmanaged.set(value)
    }

    private final Property<Boolean> reachable = objects.property(Boolean)

    @Input
    @Optional
    @Option(option = "reachable", description = """
    Analyze your source code to find which vulnerable functions and packages are called.
    """)
    Property<Boolean> getReachable() {
        reachable
    }

    void setReachable(Provider<Boolean> value) {
        reachable.set(value)
    }

    void setReachable(Boolean value) {
        reachable.set(value)
    }

    private final Property<Integer> reachableTimeout = objects.property(Integer)

    @Input
    @Optional
    Property<Integer> getReachableTimeout() {
        reachableTimeout
    }

    void setReachableTimeout(Provider<Integer> value) {
        reachableTimeout.set(value)
    }

    void setReachableTimeout(Integer value) {
        reachableTimeout.set(value)
    }

    @Option(option = "reachable-timeout", description = """
    Analyze your source code to find which vulnerable functions and packages are called.
    """)
    void reachableTimeout(String value) {
        reachableTimeout.set(Integer.parseInt(value))
    }


}
