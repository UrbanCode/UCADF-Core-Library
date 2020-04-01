/**
 * This action creates a role.
 */
package org.urbancode.ucadf.core.action.ucd.role

import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException

import groovy.json.JsonBuilder

class UcdCreateRole extends UcAdfAction {
	// Action properties.
	/** The role name. */
	String name
	
	/** The role description */
	String description = ""
	
 	/** Fail if the specified role already exists. Default is true. */
	Boolean failIfExists = true
	
	/**
	 * Runs the action.
	 * @return Returns true if it was created.
	 */
	@Override
	public Boolean run() {
		// Validate the action properties.
		validatePropsExist()

		Boolean created = false
				
		logVerbose("Creating role [$name].")
		
		WebTarget target = ucdSession.getUcdWebTarget().path("/security/role")
		logDebug("target=$target")
		
		// Build a custom post body that includes only the required fields.
		Map requestMap = [
			name: name,
			description: description
		]
		
		JsonBuilder jsonBuilder = new JsonBuilder(requestMap)

		Response response = target.request(MediaType.APPLICATION_JSON).post(Entity.json(jsonBuilder.toString()))
		if (response.getStatus() == 200) {
			logVerbose("Role [$name] created.")
			created = true
		} else {
			String errMsg = UcAdfInvalidValueException.getResponseErrorMessage(response)
			logVerbose(errMsg)
			
			Boolean alreadyExists = false
			if ((response.getStatus() == 400 || response.getStatus() == 403) && errMsg ==~ /.*already exists.*/) {
				alreadyExists = true
			} else if (response.getStatus() == 500) {
				// UCD 7.0.4 and/or 7.0.5 are returning 500 if it already exists.
				alreadyExists = true
			}
			
			if (!alreadyExists || (alreadyExists && failIfExists)) {
				throw new UcAdfInvalidValueException(errMsg)
			}
		}
		
		return created
	}
}
