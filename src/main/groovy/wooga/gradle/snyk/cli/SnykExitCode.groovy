package wooga.gradle.snyk.cli

enum SnykExitCode {

  Success("no vulnerabilities found"),
  VulnerabilitiesFound("Vulnerabilities found"),
  RerunCommand("Try to re-run command"),
  Failure_NoSupportedProjects("No supported projects detected")

  private final String message

  String getMessage() {
    message
  }

  SnykExitCode(String message) {
    this.message = message
  }
}
