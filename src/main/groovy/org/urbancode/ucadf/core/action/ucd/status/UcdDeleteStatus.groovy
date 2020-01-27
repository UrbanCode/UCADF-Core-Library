/**
 * This action deletes a status.
 */
package org.urbancode.ucadf.core.action.ucd.status

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.status.UcdStatusTypeEnum

class UcdDeleteStatus extends UcAdfAction {
	// Action properties.
	/** The status type. */
	UcdStatusTypeEnum type
	
	/** The status name or ID. */
	String status
	
	/** The flag that indicates to perform the delete, otherwise show that the delete would be done. Default is true. */
	Boolean commit = true
	
	/** The flag that indicates fail if the status is not found. Default is false. */
	Boolean failIfNotFound = false
	
	/**
	 * Runs the action.	
	 * @return True if the status was deleted.
	 */
	@Override
	public Boolean run() {
		// Validate the action properties.
		validatePropsExist()

		Boolean deleted = false
				
		if (!commit) {
			logVerbose("Would delete type [$type] status [$status].")
		} else {
			logVerbose("Deleting type [$type] status [$status].")
			
			WebTarget target = ucdSession.getUcdWebTarget().path("/cli/status")
				.queryParam("type", type)
				.queryParam("status", status)
			logDebug("target=$target")
			
			Response response = target.request().delete()
			if (response.getStatus() == 204) {
				logVerbose("Status type [$type] status [$status] deleted.")
				deleted = true
			} else {
				String errMsg = UcdInvalidValueException.getResponseErrorMessage(response)
				logVerbose(errMsg)
				// Returns a 400 error with a 'Cannot change the ghosted date once set' for the deleted statuses.
				if ((response.getStatus() != 400 && response.getStatus() != 404) || failIfNotFound) {
					throw new UcdInvalidValueException(errMsg)
				}
			}
		}
		
		return deleted
	}
}
