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
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
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
		
		logVerbose("Setting resource [$resource] property name [${ucdProperty.getName()}] secure [${ucdProperty.getSecure()}]" + (ucdProperty.getSecure() ? "." : " value [${ucdProperty.getValue()}]."))

		WebTarget target
		Response response

		// Had to add logic to handle concurrency issue discovered in UCD 7.x.
		final Integer MAXATTEMPTS = 5
		for (Integer iAttempt = 1; iAttempt <= MAXATTEMPTS; iAttempt++) {
			if (ucdSession.compareVersion(UcdSession.UCDVERSION_70) >= 0) {
				// Newer API.
				target = ucdSession.getUcdWebTarget().path("/cli/resource/setProperty")
				logDebug("target=$target")
	
				Map data = [
					resource : resource,
					name : ucdProperty.getName(),
					description : ucdProperty.getDescription(),
					isSecure : ucdProperty.getSecure(),
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
				break
			} else {
				logInfo response.readEntity(String.class)
				if (response.getStatus() == 409 && iAttempt < MAXATTEMPTS) {
					logInfo "Attempt $iAttempt failed. Waiting to try again."
					Thread.sleep(2000)
				} else {
					throw new UcAdfInvalidValueException(response)
				}
			}
		}
	}
}
