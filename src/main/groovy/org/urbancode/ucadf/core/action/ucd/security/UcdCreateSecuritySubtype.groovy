/**
 * This action creates a security subtype.
 */
package org.urbancode.ucadf.core.action.ucd.security

import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.security.UcdSecurityTypeEnum

import groovy.json.JsonBuilder

class UcdCreateSecuritySubtype extends UcAdfAction {
	// Action properties.
	/** The type to which the security subtype belongs. */
	UcdSecurityTypeEnum type
	
	/** The security subtype name. */
	String name
	
	/** The security subtype description. */
	String description = ""
	
 	/** Fail if the specified subtype already exists. Default is true. */
	Boolean failIfExists = true
	
	/** 
	 * Runs the action 
	 * @return Returns true if it was created.
	 */
	@Override
	public Boolean run() {
		// Validate the action properties.
		validatePropsExist()
		
		logInfo("Creating security type [$type] subtype [$name].")

		// Validate subtypes are allowed for the security type.
		if (!type.getSubtypeAllowed()) {
			throw new UcdInvalidValueException("Not allowed to create subtypes for security type [$type].")
		}

		Boolean created = false
				
		WebTarget target = ucdSession.getUcdWebTarget().path("/security/resourceRole")
		logDebug("target=$target")
		
		// Build a custom post body that includes only the required fields.
		Map requestMap = [ 
			name: name, 
			description: description, 
			resourceType: type.getSecurityType()
		]
		
		JsonBuilder jsonBuilder = new JsonBuilder(requestMap)
		
		Response response = target.request().post(Entity.json(jsonBuilder.toString()))
		if (response.getStatus() == 200) {
			logInfo("Security type [$type] subtype [$name] created.")
			created = true
		} else {
			String errMsg = UcdInvalidValueException.getResponseErrorMessage(response)
			logInfo(errMsg)
			if (!(response.getStatus() == 403 && (errMsg ==~ /.*already exists.*/ && !failIfExists))) {
				throw new UcdInvalidValueException(errMsg)
			}
		}
		
		return created
	}
}
