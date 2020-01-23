/**
 * This action adds application properties.
 */
package org.urbancode.ucadf.core.action.ucd.application

import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.property.UcdProperty

import com.fasterxml.jackson.annotation.JsonProperty

import groovy.json.JsonBuilder

class UcdSetApplicationProperties extends UcAdfAction {
	// Action properties.
	/** The application name or ID. */
	String application

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

		// Create the list of the properties maps.
		for (ucdProperty in ucdProperties) {
			String name = ucdProperty.getName()
			String description = ucdProperty.getDescription()
			String value = ucdProperty.getValue()
			Boolean secure = ucdProperty.getSecure()
			
			logVerbose("Setting application [$application] property [$name]" + (secure ? "." : " value [$value]."))
	
			WebTarget target = ucdSession.getUcdWebTarget().path("/cli/application/propValue")
			logDebug("target=$target")
		
			Map requestMap = [
				application : application,
				name : name, 
				description : description, 
				secure : secure, 
				value : value
			]
		
			JsonBuilder jsonBuilder = new JsonBuilder(requestMap)
			
			Response response = target.request(MediaType.APPLICATION_JSON).put(Entity.json(jsonBuilder.toString()))
			if (response.getStatus() != 200) {
				throw new UcdInvalidValueException(response)
			}
			
			logVerbose("Application [$application] property [$name] set.")
		}
	}
}
