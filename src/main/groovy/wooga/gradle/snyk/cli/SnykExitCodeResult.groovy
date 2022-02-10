package wooga.gradle.snyk.cli

enum SnykExitCodeResult {

  Success("no vulnerabilities found"),
  VulnerabilitiesFound("Vulnerabilities found"),
  RerunCommand("Try to re-run command"),
  Failure_NoSupportedProjects("No supported projects detected")

  private final String message

  String getMessage() {
    message
  }

  SnykExitCodeResult(String message) {
    this.message = message
  }
}

class SnykExecutionException extends Exception {
  private int exitCode
  private SnykExitCodeResult result
  int getExitCode() {
    exitCode
  }
  SnykExitCodeResult getResult(){
    result
  }
  SnykExecutionException(int exitCode)  {
    super(SnykExitCodeResult.values()[exitCode].message)
    this.result = SnykExitCodeResult.values()[exitCode]
    this.exitCode = exitCode
  }
}
