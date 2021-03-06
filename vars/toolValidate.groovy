import com.sap.piper.FileUtils
import com.sap.piper.Version
import hudson.AbortException

/**
 * toolValidate: validates if a tool is installed with the expected version.
 *
 * @param tool
 *           the tool to validate: java, mta, neo or cm.
 * @param home
 *           the directory containing the tool.
 */
def call(Map parameters = [:]) {

    handlePipelineStepErrors (stepName: 'toolValidate', stepParameters: parameters) {

        def tool = parameters.tool
        def home = parameters.home

        if (!tool) throw new IllegalArgumentException("The parameter 'tool' can not be null or empty.")
        if (!home) throw new IllegalArgumentException("The parameter 'home' can not be null or empty.")

        FileUtils.validateDirectoryIsNotEmpty(home)

        switch(tool) {
            case 'java': validateJava(home)
            return
            case 'mta': validateMta(home)
            return
            case 'neo': validateNeo(home)
            return
            case 'cm': validateCm(home)
            return
            default:
            throw new AbortException("The tool \'$tool\' is not supported. The following tools are supported: java, mta, neo and cm.")
        }
    }
}

def validateJava(home) {
    validateTool('Java', home, "$home/bin/java -version 2>&1", new Version(1,8,0))
}

def validateMta(home) {
    validateTool('SAP Multitarget Application Archive Builder', home, "$JAVA_HOME/bin/java -jar $home/mta.jar -v", new Version(1, 0, 6))
}

def validateNeo(home) {
    validateTool('SAP Cloud Platform Console Client', home, "$home/tools/neo.sh version", new Version(3, 39, 10))
}

def validateCm(home) {
    validateTool('Change Management Command Line Interface', home, "$home/bin/cmclient -v", new Version(0, 0, 1))
}

def validateTool(name, home, command, expectedVersion) {
    if (!name) throw new IllegalArgumentException("The parameter 'name' can not be null or empty.")
    if (!home ) throw new IllegalArgumentException("The parameter 'home' can not be null or empty.")
    if (!command ) throw new IllegalArgumentException("The parameter 'command' can not be null or empty.")
    if (!expectedVersion) throw new IllegalArgumentException("The parameter 'expectedVersion' can not be null or empty.")
    echo "[INFO] Validating $name version ${expectedVersion.toString()} or compatible version."
    def output
    try {
      output = sh returnStdout: true, script: command
    } catch(AbortException e) {
      throw new AbortException("The validation of $name failed. Please check $name home '$home': $e.message.")
    }
    def version = new Version(output)
    if (!version.isCompatibleVersion(expectedVersion)) {
      throw new AbortException("The installed version of $name is ${version.toString()}. Please install version ${expectedVersion.toString()} or a compatible version.")
    }
    echo "[INFO] $name version ${version.toString()} is installed."
}

