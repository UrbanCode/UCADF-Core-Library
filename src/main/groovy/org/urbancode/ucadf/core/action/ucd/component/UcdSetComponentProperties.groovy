/**
 * This action sets component properties.
 */
package org.urbancode.ucadf.core.action.ucd.component

import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.property.UcdProperty

import com.fasterxml.jackson.annotation.JsonProperty

import groovy.json.JsonBuilder

class UcdSetComponentProperties extends UcAdfAction {
	// Action properties.
	/** The component name or ID. */
	String component

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
			setComponentProperty(ucdProperty)
		}
	}
	
	// Set a component property.
	public setComponentProperty(final UcdProperty ucdProperty) {
		logVerbose("Setting component [$component] property [${ucdProperty.getName()}] secure [${ucdProperty.getSecure()}]" + (ucdProperty.getSecure() ? "." : " value [${ucdProperty.getValue()}]."))

		WebTarget target = ucdSession.getUcdWebTarget().path("/cli/component/propValue")
		logDebug("target=$target")

		Map requestMap = [
			component : component,
			name : ucdProperty.getName(), 
			description : ucdProperty.getDescription(), 
			isSecure : ucdProperty.getSecure(), 
			value : ucdProperty.getValue()
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
