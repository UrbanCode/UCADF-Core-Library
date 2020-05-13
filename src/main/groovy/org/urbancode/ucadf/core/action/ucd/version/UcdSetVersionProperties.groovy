/**
 * This action sets version properties.
 */
package org.urbancode.ucadf.core.action.ucd.version

import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucd.property.UcdProperty
import org.urbancode.ucadf.core.model.ucd.system.UcdSession
import org.urbancode.ucadf.core.model.ucd.version.UcdVersion

import com.fasterxml.jackson.annotation.JsonProperty

import groovy.json.JsonBuilder

class UcdSetVersionProperties extends UcAdfAction {
	// Action properties.
	/** The component name or ID. */
	String component

	/** The version name or ID. */
	String version
	
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
			setVersionProperty(ucdProperty)
		}
	}
	
	// Set version property value.
	public setVersionProperty(final UcdProperty ucdProperty) {
		logVerbose("Setting component [$component] version [$version] property [${ucdProperty.getName()}] secure [${ucdProperty.getSecure()}]" + (ucdProperty.getSecure() ? "." : " value [${ucdProperty.getValue()}]."))

		// Work around 7.0 bug where it converts a version name with 4 hyphens to a UUID.
		if (isIncorrectlyInterpretedAsUUID(version)) {
			UcdVersion ucdVersion = actionsRunner.runAction([
				action: UcdGetVersion.getSimpleName(),
				actionInfo: false,
				actionVerbose: false,
				component: component,
				version: version,
				failIfNotFound: true
			])
			
			version = ucdVersion.getId()
		}
		
		WebTarget target
		Response response

		if (ucdSession.isUcdVersion(UcdSession.UCDVERSION_70)) {
			target = ucdSession.getUcdWebTarget().path("/cli/version/versionProperties")
			logDebug("target=$target")

			Map data = [
				component : component,
				version : version,
				name : ucdProperty.getName(), 
				isSecure : ucdProperty.getSecure(), 
				value : ucdProperty.getValue()
			]

			JsonBuilder jsonBuilder = new JsonBuilder(data)
			
			response = target.request(MediaType.APPLICATION_JSON).put(Entity.json(jsonBuilder.toString()))
		} else {
			target = ucdSession.getUcdWebTarget().path("/cli/version/versionProperties")
				.queryParam("component", component)
				.queryParam("version", version)
				.queryParam("name", ucdProperty.getName())
				.queryParam("value", ucdProperty.getValue())
				.queryParam("isSecure", ucdProperty.getSecure())
			logDebug("target=$target")
			
			response = target.request(MediaType.APPLICATION_JSON).put(Entity.json(""))
		}
		
		if (response.getStatus() == 200) {
			logVerbose("Property [${ucdProperty.getName()}] set.")
		} else {
            throw new UcAdfInvalidValueException(response)
		}
	}	
}
