package wooga.gradle.snyk

import com.wooga.gradle.BaseSpec
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.options.Option

trait SnykInstallSpec extends BaseSpec {

    @Internal
    ProviderFactory getProviderFactory() {
        getProviders()
    }

    private final DirectoryProperty installationDir = objects.directoryProperty()

    @Internal

    DirectoryProperty getInstallationDir() {
        installationDir
    }

    void setInstallationDir(Provider<Directory> value) {
        installationDir.set(value)
    }

    void setInstallationDir(File value) {
        installationDir.set(value)
    }

    @Option(option = "installation-dir", description = """
    The directory to install to.
    """)
    void setInstallationDir(String value) {
        installationDir.set(new File(value))
    }

    private final Property<String> executableName = objects.property(String)

    @Internal
    @Option(option = "executable-name", description = """
    The name of the executable without .exe after installation.
    """)
    Property<String> getExecutableName() {
        executableName
    }

    void setExecutableName(Provider<String> value) {
        executableName.set(value)
    }

    void setExecutableName(String value) {
        executableName.set(value)
    }

    private final Property<String> version = objects.property(String)

    @Input
    @Option(option = "version", description = """
    The version to install.
    """)
    Property<String> getVersion() {
        version
    }

    void setVersion(Provider<String> value) {
        version.set(value)
    }

    void setVersion(String value) {
        version.set(value)
    }
}
