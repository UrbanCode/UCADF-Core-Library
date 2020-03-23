/**
 * This action sets application process request properties.
 */
package org.urbancode.ucadf.core.action.ucd.applicationProcessRequest

import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucd.property.UcdProperty

import com.fasterxml.jackson.annotation.JsonProperty

import groovy.json.JsonBuilder

class UcdSetApplicationProcessRequestProperties extends UcAdfAction {
	// Action properties.
	/** The application process request ID. */
	String requestId
	
	/** The list of application process request properties. */
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
			
			logVerbose("Setting application process request [$requestId] property [$name] " + (secure ? "." : "value [$value]."))
			
			data.add([
				name: name,
				value: value,
				secure: secure.toString()
			])
		}
		
		JsonBuilder jsonBuilder = new JsonBuilder(data)

		// Had to add logic to handle concurrency issue discovered in UCD 7.x.
		final Integer MAXATTEMPTS = 5
		for (Integer iAttempt = 1; iAttempt <= MAXATTEMPTS; iAttempt++) {
			WebTarget target = ucdSession.getUcdWebTarget().path("/rest/deploy/applicationProcessRequest/{requestId}/saveProperties")
				.resolveTemplate("requestId", requestId)
			logDebug("target=$target")
			
			Response response = target.request(MediaType.APPLICATION_JSON).put(Entity.json(jsonBuilder.toString()))
			if (response.getStatus() == 200) {
				break
			} else {
				logInfo response.readEntity(String.class)
				if (response.getStatus() == 409 && iAttempt < MAXATTEMPTS) {
					logInfo "Attempt $iAttempt failed. Waiting to try again."
					Thread.sleep(2000)
				} else {
					throw new UcAdfInvalidValueException("Status: ${response.getStatus()} Unable to set application process request property. $target")
				}
			}
		}
	}
}
