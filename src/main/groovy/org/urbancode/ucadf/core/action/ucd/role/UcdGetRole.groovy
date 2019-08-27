/**
 * This action gets a role.
 */
package org.urbancode.ucadf.core.action.ucd.role

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.role.UcdRole

class UcdGetRole extends UcAdfAction {
	// Action properties.
	/** The role name or ID. */
	String role
	
	/** The flag that indicates fail if the role is not found. Default is true. */
	Boolean failIfNotFound = true
	
	/**
	 * Runs the action.
	 * @return Returns a role object.
	 */
	@Override
	public UcdRole run() {
		// Validate the action properties.
		validatePropsExist()

		UcdRole ucdRole
		
		WebTarget target = ucdSession.getUcdWebTarget().path("/security/role/{role}")
			.resolveTemplate("role", role)
		logDebug("target=$target")
		
		Response response = target.request().get()
		if (response.getStatus() == 200) {
			ucdRole = response.readEntity(UcdRole.class)
		} else {
			String errMsg = UcdInvalidValueException.getResponseErrorMessage(response)
			logInfo(errMsg)
			if ((response.getStatus() != 404 && response.getStatus() != 500) || failIfNotFound) {
				throw new UcdInvalidValueException(errMsg)
			}
		}
		
		return ucdRole
	}
}
