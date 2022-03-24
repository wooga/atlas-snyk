package wooga.gradle.snyk.tasks


import static com.wooga.gradle.PlatformUtils.*
import static wooga.gradle.snyk.tasks.SnykInstallBaseIntegrationSpec.InstallTestModel.model

class SnykToHtmlInstallIntegrationSpec extends SnykInstallBaseIntegrationSpec<SnykToHtmlInstall> {

    List<InstallTestModel> getInstallTestVersions() {
        List<InstallTestModel> data = []
        data << model("2.2.2", "snyk", "s2html", new File("snyk/s2html"))
        data << model("2.2.1", "snyk", "snyk2html", new File("snyk/snyk2html"))
        data << model("2.0.0", "snuk", "snyk-to-html", new File("snuk/snyk-to-html"))

        if (isMac()) {
            data[0].expectedChecksum = "feb674cf2d72832e24600b03ac75108dfeae99760754048d04db6f6248b5aaa2"
            data[1].expectedChecksum = "c584b76e718773af76378b40604ebfb901be4afafa33ce7b7890e5df431924de"
            data[2].expectedChecksum = "e52321bc5306b141837fdcdb508ae7a79d83c10bf711f5d0b3b9f098e7e26d86"
        }
        if (isWindows()) {
            data[0].expectedChecksum = "850c9d402f4d50765aaee49107f3dcc24962f45e012ef92c85589351317fa759"
            data[1].expectedChecksum = "5599ecc29f0b834f7673f723ef72f96c1d54d1ed9e394772aaf33eb9aa8f26f8"
            data[2].expectedChecksum = "43e497b135ed646d8ef640a14dc3e6531d185a936e59f1fba329780178538b75"
        }

        if (isLinux()) {
            data[0].expectedChecksum = "8bfb615f624072a283bdf2c1d4a8f1a6ac8f2e828daf55d1b6e19676d3ac409f"
            data[1].expectedChecksum = "2e443ffe87d9bc4512e8ba2d37c13a5425d5e8de985f8ce95920903f13b16b93"
            data[2].expectedChecksum = "daa8fc1fe0863d7ec4f3bf82eb3ce9a19ed4957ef054cf6d890d6164839c5585"
        }
        data
    }

    @Override
    String downloadRequestPath(String version, String file) {
        "/snyk-to-html/${ensureVersionPrefix(version)}/${file}"
    }

    @Override
    List<String> getVersionFiles() {
        def files = []
        if (isMac()) {
            files << "snyk-to-html-macos" << "snyk-to-html-macos.sha256"
        }
        if (isWindows()) {
            files << "snyk-to-html-win.exe" << "snyk-to-html-win.exe.sha256"
        }
        if (isLinux()) {
            files << "snyk-to-html-linux" << "snyk-to-html-linux.sha256"
        }
        files
    }
}
