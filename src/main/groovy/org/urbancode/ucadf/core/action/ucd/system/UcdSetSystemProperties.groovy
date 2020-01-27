/**
 * This action sets system properties.
 */
package org.urbancode.ucadf.core.action.ucd.system

import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.property.UcdProperty
import org.urbancode.ucadf.core.model.ucd.system.UcdSession

import groovy.json.JsonBuilder

class UcdSetSystemProperties extends UcAdfAction {
	// Action properties.
	/** The list of system properties. */
	List<UcdProperty> systemProperties
	
	/**
	 * Runs the action.	
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExist()

		// Sets a list of system properties.
		for (ucdProperty in systemProperties) {
			logVerbose("Setting system property [${ucdProperty.getName()}].")
	
			WebTarget target
			Response response
					
			if (ucdSession.isUcdVersion(UcdSession.UCDVERSION_70)) {
				target = ucdSession.getUcdWebTarget().path("/cli/systemConfiguration/propValue")
				logDebug("target=$target")
		
				Map requestMap = [
					isSecure : ucdProperty.getSecure(),
					name : ucdProperty.getName(),
					value : ucdProperty.getValue()
				]
		
				JsonBuilder jsonBuilder = new JsonBuilder(requestMap)
		
				response = target.request(MediaType.APPLICATION_JSON).put(Entity.json(jsonBuilder.toString()))
			} else {
				target = ucdSession.getUcdWebTarget().path("/cli/systemConfiguration/propValue")
					.queryParam("name", ucdProperty.getName())
					.queryParam("value", ucdProperty.getValue())
					.queryParam("isSecure", ucdProperty.getSecure())
					
				response = target.request(MediaType.APPLICATION_JSON).put(Entity.json(""))
			}
			
			if (response.getStatus() == 200) {
				logVerbose("System property set.")
			} else {
				throw new UcdInvalidValueException(response)
			}
		}
	}
}
