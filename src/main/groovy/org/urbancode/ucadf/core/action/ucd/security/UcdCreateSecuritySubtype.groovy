/**
 * This action creates a security subtype.
 */
package org.urbancode.ucadf.core.action.ucd.security

import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
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
		
		logVerbose("Creating security type [$type] subtype [$name].")

		// Validate subtypes are allowed for the security type.
		if (!type.getSubtypeAllowed()) {
			throw new UcAdfInvalidValueException("Not allowed to create subtypes for security type [$type].")
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
			logVerbose("Security type [$type] subtype [$name] created.")
			created = true
		} else {
			String errMsg = UcAdfInvalidValueException.getResponseErrorMessage(response)
			logVerbose(errMsg)
			
			Boolean alreadyExists = false
			if ((response.getStatus() == 400 || response.getStatus() == 403) && errMsg ==~ /.*already exists.*/) {
				alreadyExists = true
			} else if (response.getStatus() == 500 && errMsg ==~ /.*after response has been committed.*/) {
				// UCD 7.0.4 is returning 500 Cannot forward after response has been committed if it already exists.
				alreadyExists = true
			}
			
			if (!alreadyExists || (alreadyExists && failIfExists)) {
				throw new UcAdfInvalidValueException(errMsg)
			}
		}
		
		return created
	}
}
