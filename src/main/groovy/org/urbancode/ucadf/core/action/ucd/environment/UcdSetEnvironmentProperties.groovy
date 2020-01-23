/**
 * This action sets an environment's properties.
 */
package org.urbancode.ucadf.core.action.ucd.environment

import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.property.UcdProperty

import com.fasterxml.jackson.annotation.JsonProperty

import groovy.json.JsonBuilder

class UcdSetEnvironmentProperties extends UcAdfAction {
	// Action properties.
	/** The application name or ID. */
	String application
	
	/** The environment name or ID. */
	String environment

	/** The list of properties. */	
	@JsonProperty("properties")
	List<UcdProperty> ucdProperties
	
	/**
	 * Runs the action.	
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExist()
		
		// Process each property.
		for (ucdProperty in ucdProperties) {
			logVerbose("Setting application [$application] environment [$environment] property name [${ucdProperty.getName()}] secure [${ucdProperty.getSecure()}]" + (ucdProperty.getSecure() ? "." : " value [${ucdProperty.getValue()}]."))
	
			WebTarget target = ucdSession.getUcdWebTarget().path("/cli/environment/propValue")
			logDebug("target=$target")
	
			Map requestMap = [
				application: application,
				environment: environment,
				name: ucdProperty.getName(),
				description: ucdProperty.getDescription(),
				isSecure: ucdProperty.getSecure(),
				value: ucdProperty.getValue()
			]
	
			JsonBuilder jsonBuilder = new JsonBuilder(requestMap)
	
			Response response = target.request(MediaType.APPLICATION_JSON).put(Entity.json(jsonBuilder.toString()))
			if (response.getStatus() == 200) {
				logVerbose("Property [${ucdProperty.getName()}] set.")
			} else {
	            throw new UcdInvalidValueException(response)
			}
		}
	}
}
