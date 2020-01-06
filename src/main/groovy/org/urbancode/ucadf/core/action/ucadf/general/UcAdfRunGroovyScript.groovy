/**
 * This action is used to sleep for the specified number of seconds.
 */
package org.urbancode.ucadf.core.action.ucadf.general

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException

class UcAdfRunGroovyScript extends UcAdfAction {
	/** (Optional) The groovy text to run. */
	String scriptText = ""

	/** (Optional) The name of a file containing the groovy text to run. */
	String scriptFileName = ""
		
	/**
	 * Runs the action.	
	 * @return The return from the Groovy script.
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExist()

		// If a script file was specified then read the script text from that file.
		String derivedScriptBody
		if (scriptFileName) {
			scriptText = new File(scriptFileName).text
		}

		// Prepare to bind the variables to the script.
		Binding binding = new Binding()
		binding.setVariable("action", this)
		
		// Run the script.
		return actionsRunner.runGroovyScript(
			binding, 
			scriptText
		)
	}
}
