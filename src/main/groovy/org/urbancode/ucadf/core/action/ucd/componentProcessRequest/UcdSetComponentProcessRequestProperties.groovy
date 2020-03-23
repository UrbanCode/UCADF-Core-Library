/**
 * This action sets component process request properties.
 */
package org.urbancode.ucadf.core.action.ucd.componentProcessRequest

import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucd.property.UcdProperty

import com.fasterxml.jackson.annotation.JsonProperty

import groovy.json.JsonBuilder

class UcdSetComponentProcessRequestProperties extends UcAdfAction {
	// Action properties.
	/** The application Process request ID. */
	String requestId
	
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
		ArrayList data = []
		for (ucdProperty in ucdProperties) {
			String name = ucdProperty.getName()
			String value = ucdProperty.getValue()
			Boolean secure = ucdProperty.getSecure()
			
			logVerbose("Setting component process request [$requestId] property [$name] " + (secure ? "." : "value [$value]."))
			
			data.add([
				name: name,
				value: value,
				secure: secure.toString()
			])
		}
		
		JsonBuilder jsonBuilder = new JsonBuilder(data)

		WebTarget target = ucdSession.getUcdWebTarget().path("/rest/deploy/componentProcessRequest/{requestId}/saveProperties")
			.resolveTemplate("requestId", requestId)
		logDebug("target=$target")
		
		Response response = target.request(MediaType.APPLICATION_JSON).put(Entity.json(jsonBuilder.toString()))
		if (response.getStatus() != 200) {	
            throw new UcAdfInvalidValueException(response)
		}
	}
}
