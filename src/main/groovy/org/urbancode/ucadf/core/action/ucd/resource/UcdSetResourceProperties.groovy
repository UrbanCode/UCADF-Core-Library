/**
 * This action sets resource properties.
 */
package org.urbancode.ucadf.core.action.ucd.resource

import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.glassfish.jersey.uri.UriComponent
import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.property.UcdProperty
import org.urbancode.ucadf.core.model.ucd.system.UcdSession

import com.fasterxml.jackson.annotation.JsonProperty

import groovy.json.JsonBuilder

class UcdSetResourceProperties extends UcAdfAction {
	// Action properties.
	/** The resource path or ID. */
	String resource
	
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
			setResourceProperty(ucdProperty)
		}
	}
	
	// Set a resource property.
	public setResourceProperty(
		final UcdProperty ucdProperty) {
		
		logInfo("Setting resource [$resource] property name [${ucdProperty.getName()}] secure [${ucdProperty.getSecure()}]" + (ucdProperty.getSecure() ? "." : " value [${ucdProperty.getValue()}]."))

		WebTarget target
		Response response

		if (ucdSession.isUcdVersion(UcdSession.UCDVERSION_70)) {
			// Newer API.
			target = ucdSession.getUcdWebTarget().path("/cli/resource/setProperty")
			logDebug("target=$target")

			Map data = [
				resource : resource,
				name : ucdProperty.getName(),
				description : ucdProperty.getDescription(),
				secure : ucdProperty.getSecure(),
				value : ucdProperty.getValue()
			]

			JsonBuilder jsonBuilder = new JsonBuilder(data)
			
			response = target.request(MediaType.APPLICATION_JSON).put(Entity.json(jsonBuilder.toString()))
		} else {
			// Older API.
			String encodedValue = UriComponent.encode(
				ucdProperty.getValue(),
				UriComponent.Type.QUERY_PARAM_SPACE_ENCODED
			)
			
			target = ucdSession.getUcdWebTarget().path("/cli/resource/setProperty")
				.queryParam("resource", resource)
				.queryParam("name", ucdProperty.getName())
				.queryParam("value", encodedValue)
				.queryParam("isSecure", ucdProperty.getSecure())
			logDebug("target=$target")
	
			response = target.request().put(Entity.text(""))
		}
		
		if (response.getStatus() == 200) {
			logInfo("Property [${ucdProperty.getName()}] set.")
		} else {
			throw new UcdInvalidValueException(response)
		}
	}
}
