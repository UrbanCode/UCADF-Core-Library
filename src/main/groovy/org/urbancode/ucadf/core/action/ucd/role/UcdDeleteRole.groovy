/**
 * This action deletes a role.
 */
package org.urbancode.ucadf.core.action.ucd.role

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException

class UcdDeleteRole extends UcAdfAction {
	// Action properties.
	/** The role name or ID */
	String role
	
	/** The flag that indicates to perform the delete, otherwise show that the delete would be done. Default is true. */
	Boolean commit = true
	
 	/** The flag that indicates fail if the specified role is not found. Default is false. */
	Boolean failIfNotFound = false
	
	/**
	 * Runs the action.
	 * @return Returns true if it was deleted.
	 */
	@Override
	public Boolean run() {
		// Validate the action properties.
		validatePropsExist()

		Boolean deleted = false
		
		if (commit) {
			logVerbose("Deleting role [$role]")
			WebTarget target = ucdSession.getUcdWebTarget().path("/security/role/{roleName}")
				.resolveTemplate("roleName", role)
			logDebug("target=$target")

			Response response = target.request(MediaType.WILDCARD).delete()
			if (response.getStatus() == 200) {
				logVerbose("Role [$role] deleted.")
				deleted = true
			} else {
				String errMsg = UcdInvalidValueException.getResponseErrorMessage(response)
				logVerbose(errMsg)
				if ((response.getStatus() != 404 && response.getStatus() != 500) || failIfNotFound) {
					throw new UcdInvalidValueException(errMsg)
				}
			}
		} else {
			logVerbose("Would delete role [$role].")
		}
		
		return deleted
	}
}
