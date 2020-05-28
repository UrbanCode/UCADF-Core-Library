/**
 * This action gets an application template process.
 */
package org.urbancode.ucadf.core.action.ucd.applicationTemplateProcess

import org.urbancode.ucadf.core.action.ucd.applicationProcess.UcdGetApplicationProcess
import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucd.applicationProcess.UcdApplicationProcess
import org.urbancode.ucadf.core.model.ucd.general.UcdObject

class UcdGetApplicationTemplateProcess extends UcAdfAction {
	// Action properties.
	/** The application template name or ID. If not specified then process must be an ID. */
	String applicationTemplate = ""
	
	/** The version of the application template. Default is -1. */	
	Long templateVersion = -1
	
	/** The process name or ID. */
	String process

	/** The version of the process. Default is -1. */	
	Long processVersion = -1
	
	/** The flag that indicates fail if the application template process is not found. Default is true. */
	Boolean failIfNotFound = true
	
	/**
	 * Runs the action.	
	 * @return The application process object.
	 */
	@Override
	public UcdApplicationProcess run() {
		// Validate the action properties.
		validatePropsExist()

		logVerbose("Getting application template [$applicationTemplate] process [$process].")
	
		UcdApplicationProcess ucdApplicationProcess 

		// If the ID was specified then get the application process using the ID.
		String applicationTemplateId
		if (UcdObject.isUUID(process)) {
			ucdApplicationProcess = actionsRunner.runAction([
				action: UcdGetApplicationProcess.getSimpleName(),
				actionInfo: false,
				process: process,
				version: processVersion,
				failIfNotFound: failIfNotFound
			])
		} else {
			// Don't know a way to look up an application template process by name so get the list and find it.
			List<UcdApplicationProcess> ucdApplicationProcesses = actionsRunner.runAction([
				action: UcdGetApplicationTemplateProcesses.getSimpleName(),
				actionInfo: false,
				applicationTemplate: applicationTemplate,
				version: templateVersion
			])
			
			ucdApplicationProcess = ucdApplicationProcesses.find {
				it.getName().equals(process)
			}

			if (!ucdApplicationProcess && failIfNotFound) {
				throw new UcAdfInvalidValueException("Application template [$applicationTemplate] process [$process] not found.")
			}
			
			// Get the application process by ID to get the specified version.
			ucdApplicationProcess = actionsRunner.runAction([
				action: UcdGetApplicationProcess.getSimpleName(),
				actionInfo: false,
				process: ucdApplicationProcess.getId(),
				version: processVersion,
				failIfNotFound: failIfNotFound
			])
		}
		
		return ucdApplicationProcess
	}
}
