/**
 * This action is used to run a shell command.
 */
package org.urbancode.ucadf.core.action.ucadf.general

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException

class UcAdfExecuteCommand extends UcAdfAction {
	/** The command list. One item for each command word, e.g. "ls", "-l" would be two items. */
	List<String> commandList
	
	/** The maximum number of seconds to wait. */
	Integer maxWaitSecs = 600
	
	/** Flag that indicates to show the processing output. */
	Boolean showOutput = false
	
	/** Flag to indicate to throw an exception if the command fails. */
	Boolean throwException = true

	/**
	 * Runs the action.	
	 * @return The command return information.
	 */
	@Override
	public UcAdfExecuteCommandReturn run() {
		// Validate the action properties.
		validatePropsExist()

		UcAdfExecuteCommandReturn executeCommandReturn = new UcAdfExecuteCommandReturn()
		
		// Convert list to string array
		String[] commandArr = new String[commandList.size()]
		
		// Replace any null elements with empty string
		for (int i = 0; i < commandList.size(); i++) {
			if (commandList[i] == null) {
				commandArr[i] = ""
			} else {
				commandArr[i] = commandList[i]
			}
		}

		// Initiate the execution
		Process process = Runtime.getRuntime().exec(commandArr)

		// Wait on the execution and get the results
		StringBuffer stdOut = new StringBuffer()
		StringBuffer stdErr = new StringBuffer()
		process.consumeProcessOutput(stdOut, stdErr)
		process.waitForOrKill(maxWaitSecs * 1000)
		int exitValue = process.exitValue()
		if (showOutput) {
			if (stdOut) {
				println "<<<STDOUT>>>\n$stdOut"
			}
			if (stdErr) {
				println "<<<STDERR>>>\n$stdErr"
			}
		}
		if (exitValue && throwException) {
			throw new UcdInvalidValueException("Command failed.")
		}

		// Set the return object values.		
		executeCommandReturn.setStdOut(stdOut.toString())
		executeCommandReturn.setStdErr(stdErr.toString())
		executeCommandReturn.setExitValue(exitValue)
		
		return executeCommandReturn
	}

	// The action return information.
	static public class UcAdfExecuteCommandReturn {
		String stdOut
		String stdErr
		Integer exitValue
	}
}
