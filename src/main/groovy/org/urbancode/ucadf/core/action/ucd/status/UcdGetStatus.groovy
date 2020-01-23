/**
 * This action get a status.
 */
package org.urbancode.ucadf.core.action.ucd.status

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.status.UcdStatus
import org.urbancode.ucadf.core.model.ucd.status.UcdStatusTypeEnum

class UcdGetStatus extends UcAdfAction {
	// Action properties.
	/** The status type. */
	UcdStatusTypeEnum type
	
	/** The status name or ID. */
	String status
	
	/** The flag that indicates include deleted statuses. */
	Boolean includeDeleted = false
	
	/** The flag that indicates fail if the status is not found. Default is true. */
	Boolean failIfNotFound = true
	
	/**
	 * Runs the action.	
	 * @return The status object.
	 */
	@Override
	public UcdStatus run() {
		// Validate the action properties.
		validatePropsExist()

		UcdStatus ucdStatus

		logVerbose("Getting type [$type] status [$status].")
				
		WebTarget target = ucdSession.getUcdWebTarget().path("/cli/status/getStatus")
			.queryParam("type", type)
			.queryParam("status", status)
		logDebug("target=$target")
		
		Response response = target.request().get()
		if (response.getStatus() == 200) {
			ucdStatus = response.readEntity(UcdStatus.class)
		
			// Handle return of deleted status.
			if (ucdStatus.getDeleted() == true && !includeDeleted) {
				ucdStatus = null
			}
		} else {
			String errMsg = UcdInvalidValueException.getResponseErrorMessage(response)
			logVerbose(errMsg)
			if (response.getStatus() != 400 || failIfNotFound) {
				throw new UcdInvalidValueException(errMsg)
			}
		}
		
		return ucdStatus
	}
}
