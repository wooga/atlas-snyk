package wooga.gradle.snyk.cli

import com.wooga.gradle.BaseSpec
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.options.Option

trait SnykTestOutputSpec extends BaseSpec {
    private final RegularFileProperty jsonOutputPath = objects.fileProperty()

    @OutputFile
    @Optional
    RegularFileProperty getJsonOutputPath() {
        jsonOutputPath
    }

    void setJsonOutputPath(Provider<RegularFile> value) {
        jsonOutputPath.set(value)
    }

    void setJsonOutputPath(File value) {
        jsonOutputPath.set(value)
    }

    void setJsonOutputPath(String value) {
        jsonOutputPath.set(new File(value))
    }

    @Option(option = "json-file-output", description = """
    Save test output in JSON format directly to the specified file, regardless of whether or not you
    use the --json option.
    This is especially useful if you want to display the human-readable test output using stdout and
    at the same time save the JSON format output to a file.
    """)
    void jsonOutputPath(String path) {
        jsonOutputPath.set(new File(path))
    }

    private final RegularFileProperty sarifOutputPath = objects.fileProperty()

    @OutputFile
    @Optional
    RegularFileProperty getSarifOutputPath() {
        sarifOutputPath
    }

    void setSarifOutputPath(Provider<RegularFile> value) {
        sarifOutputPath.set(value)
    }

    void setSarifOutputPath(File value) {
        sarifOutputPath.set(value)
    }

    void setSarifOutputPath(String path) {
        sarifOutputPath.set(new File(path))
    }

    @Option(option = "sarif-file-output", description = """
    Save test output in SARIF format directly to the <OUTPUT_FILE_PATH> file, regardless of whether
    or not you use the --sarif option.
    This is especially useful if you want to display the human-readable test output using stdout and
    at the same time save the SARIF format output to a file.
    """)
    void sarifOutputPath(String path) {
        sarifOutputPath.set(new File(path))
    }
}
