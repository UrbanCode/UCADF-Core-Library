/**
 * This action deletes an application process.
 */
package org.urbancode.ucadf.core.action.ucd.applicationProcess

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.applicationProcess.UcdApplicationProcess
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.general.UcdObject

class UcdDeleteApplicationProcess extends UcAdfAction {
	// Action properties.
	/** The application name or ID. */
	String application
	
	/** The process name or ID. */
	String process
	
	/** The flag that indicates fail if the application process is not found. Default is false. */
	Boolean failIfNotFound = false

	// Private properties.
	private Boolean deleted = false
		
	/**
	 * Runs the action.	
	 * @return True if the application process was deleted.
	 */
	@Override
	public Boolean run() {
		// Validate the action properties.
		validatePropsExist()
		
		logVerbose("Deleting application [$application] process [$process].")

		if (UcdObject.isUUID(process)) {
			deleteApplicationProcess(process)
		} else {
			UcdApplicationProcess ucdApplicationProcess = actionsRunner.runAction([
				action: UcdGetApplicationProcess.getSimpleName(),
				actionInfo: actionInfo,
				application: application,
				process: process,
				failIfNotFound: failIfNotFound
			])
			
			if (ucdApplicationProcess) {
				deleteApplicationProcess(ucdApplicationProcess.getId())
			}
		}
		
		return deleted
	}

	// Delete the application process.	
	public deleteApplicationProcess(final String processId) {
		WebTarget target = ucdSession.getUcdWebTarget().path("/rest/deploy/applicationProcess/{processId}")
			.resolveTemplate("processId", processId)
		logDebug("target=$target")
		
		Response response = target.request(MediaType.APPLICATION_JSON).delete()
		if (response.getStatus() == 204) {
			logVerbose("Application [$application] process [$process] deleted.")
			deleted = true
		} else {
			String errMsg = UcdInvalidValueException.getResponseErrorMessage(response)
			logVerbose(errMsg)
			if (response.getStatus() != 404 || failIfNotFound) {
				throw new UcdInvalidValueException(errMsg)
			}
		}
	}
}
