/**
 * This action sets an environment's properties.
 */
package org.urbancode.ucadf.core.action.ucd.environment

import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
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
	
			// Had to add logic to handle concurrency issue discovered in UCD 7.x.
			final Integer MAXATTEMPTS = 10
			for (Integer iAttempt = 1; iAttempt <= MAXATTEMPTS; iAttempt++) {
				Response response = target.request(MediaType.APPLICATION_JSON).put(Entity.json(jsonBuilder.toString()))
				if (response.getStatus() == 200) {
					logVerbose("Property [${ucdProperty.getName()}] set.")
					break
				} else {
					String responseStr = response.readEntity(String.class)
					logInfo(responseStr)
					if ((response.getStatus() == 409 || responseStr.matches(/.*transaction.*/)) && iAttempt < MAXATTEMPTS) {
						logWarn("Attempt $iAttempt failed. Waiting to try again.")
						Random rand = new Random(System.currentTimeMillis())
						Thread.sleep(rand.nextInt(2000))
					} else {
						throw new UcAdfInvalidValueException("Status: ${response.getStatus()} Unable to set application process request property. $target")
					}
				}
			}
		}
	}
}
