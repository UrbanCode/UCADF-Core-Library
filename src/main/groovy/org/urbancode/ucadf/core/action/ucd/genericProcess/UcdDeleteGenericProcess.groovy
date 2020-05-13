/**
 * This action deletes a generic process.
 */
package org.urbancode.ucadf.core.action.ucd.genericProcess

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucd.genericProcess.UcdGenericProcess

class UcdDeleteGenericProcess extends UcAdfAction {
	// Action properties.
	/** The generic process name or ID. */
	String process
	
	/** The flag that indicates to perform the delete, otherwise show that the delete would be done. Default is true. */
	Boolean commit = true
	
	/** The flag that indicates fail if the generic process is not found. Default is false. */
	Boolean failIfNotFound = false
	
	/**
	 * Runs the action.	
	 * @return True if the generic process was deleted.
	 */
	@Override
	public Boolean run() {
		// Validate the action properties.
		validatePropsExist()

		Boolean deleted = false
				
		logVerbose("Deleting generic process [$process] commit [$commit].")

		if (commit) {
			UcdGenericProcess ucdGenericProcess = actionsRunner.runAction([
				action: UcdGetGenericProcess.getSimpleName(),
				actionInfo: false,
				actionVerbose: false,
				process: process,
				failIfNotFound: failIfNotFound
			])

			if (ucdGenericProcess) {
				WebTarget target = ucdSession.getUcdWebTarget().path("/rest/process/{processId}")
					.resolveTemplate("processId", ucdGenericProcess.getId())
				logDebug("target=$target")
				
				Response response = target.request(MediaType.APPLICATION_JSON).delete()
				if (response.getStatus() == 204) {
					logVerbose("Generic [$process] deleted.")
					deleted = true
				} else {
					throw new UcAdfInvalidValueException(response)
				}
			}
		} else {
			logVerbose("Would delete generic process [$process].")
		}
		
		return deleted
	}	
}
