/**
 * This action deletes a component process.
 */
package org.urbancode.ucadf.core.action.ucd.componentProcess

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.componentProcess.UcdComponentProcess
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucd.general.UcdObject

class UcdDeleteComponentProcess extends UcAdfAction {
	// Action properties.
	/** The component name or ID. */
	String component

	/** The process name or ID. */	
	String process
	
	/** The flag that indicates fail if the component process is not found. Default is false. */
	Boolean failIfNotFound = false
	
	/**
	 * Runs the action.	
	 * @return True if the component process was deleted.
	 */
	@Override
	public Boolean run() {
		// Validate the action properties.
		validatePropsExist()

		Boolean deleted = false
				
		logVerbose("Deleting component [$component] process [$process].")

		String processId
		if (UcdObject.isUUID(process)) {
			processId = process
		} else {
			UcdComponentProcess ucdComponentProcess = actionsRunner.runAction([
				action: UcdGetComponentProcess.getSimpleName(),
				actionInfo: false,
				actionVerbose: false,
				component: component,
				process: process,
				failIfNotFound: failIfNotFound
			])
			
			if (ucdComponentProcess) {
				processId = ucdComponentProcess.getId()
			} else {
				 if (failIfNotFound) {
					 throw new UcAdfInvalidValueException("Component [$component] process [$process] not found.")
				 }
			}
			
		}

		if (processId) {		
			WebTarget target = ucdSession.getUcdWebTarget().path("/rest/deploy/componentProcess/{processId}")
				.resolveTemplate("processId", processId)
			logDebug("target=$target")
			
			Response response = target.request(MediaType.APPLICATION_JSON).delete()
			if (response.getStatus() == 204) {
				logVerbose("Component [$component] process [$process] deleted.")
				deleted = true
			} else {
				String errMsg = UcAdfInvalidValueException.getResponseErrorMessage(response)
				logVerbose(errMsg)
				if (response.getStatus() != 404 || failIfNotFound) {
					throw new UcAdfInvalidValueException(errMsg)
				}
			}
		}
		
		return deleted
	}
}
