/**
 * This action creates a role.
 */
package org.urbancode.ucadf.core.action.ucd.role

import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException

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
			String errMsg = UcdInvalidValueException.getResponseErrorMessage(response)
			logVerbose(errMsg)
			
			Boolean alreadyExists = false
			if (response.getStatus() == 403 && errMsg ==~ /.*already exists.*/) {
				alreadyExists = true
			} else if (response.getStatus() == 500 && errMsg ==~ /.*after response has been committed.*/) {
				// UCD 7.0.4 is returning 500 Cannot forward after response has been committed if it already exists.
				alreadyExists = true
			}
			
			if (!alreadyExists || (alreadyExists && failIfExists)) {
				throw new UcdInvalidValueException(errMsg)
			}
		}
		
		return created
	}
}
