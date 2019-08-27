/**
 * This action synchronizes all of the application processes in the to application with the processes in the from application.
 */
package org.urbancode.ucadf.core.action.ucd.applicationProcess

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.applicationProcess.UcdApplicationProcess

// Sync all of the application processes from on application to another.
// This will delete additional processes that exist in toApplication.
class UcdSyncApplicationProcesses extends UcAdfAction {
	// Action properties.
	/** The from application name or ID. */
	String fromApplication
	
	/** The to application name or ID. */
	String toApplication
	
	/**
	 * Runs the action.	
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExist()

		logInfo("Synchronize application processes from [$fromApplication] to [$toApplication].")

		// Copy all application processes from one application to another.		
		List<UcdApplicationProcess> fromProcesses = actionsRunner.runAction([
			action: UcdCopyApplicationProcesses.getSimpleName(),
			fromApplication: fromApplication,
			toApplication: toApplication,
			replaceProcess: true
		])

		List<UcdApplicationProcess> toProcesses = actionsRunner.runAction([
			action: UcdGetApplicationProcesses.getSimpleName(),
			application: toApplication
		])

        for (toProcess in toProcesses) {
            UcdApplicationProcess fromProcess = fromProcesses.find { it.getName().equals(toProcess.getName()) }
            if (!fromProcess) {
                logInfo("Deleting application process [${toProcess.getName()}] that does not exist in from application.")
				actionsRunner.runAction([
					action: UcdDeleteApplicationProcess.getSimpleName(),
					application: toApplication,
					process: toProcess.getId(),
					failIfNotFound: false
				])
            }
        }
    }
}
