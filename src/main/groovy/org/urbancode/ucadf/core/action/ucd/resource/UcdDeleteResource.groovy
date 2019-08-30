/**
 * This action deletes a resource.
 */
package org.urbancode.ucadf.core.action.ucd.resource

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException

class UcdDeleteResource extends UcAdfAction {
	// Action properties.
	/** The resource path or ID. */
	String resource
	
	/** The flag that indicates to perform the delete, otherwise show that the delete would be done. Default is true. */
	Boolean commit = true
	
	/**
	 * Runs the action.	
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExist()
		
		if (!commit) {
			logInfo("Would delete resource [$resource] from ucdUrl [${ucdSession.getUcdUrl()}].")
		} else {
			logInfo("Deleting Resource [$resource].")

			// Had to add logic to handle concurrency issue discovered in UCD 6.2.7.0.
			final Integer MAXATTEMPTS = 5
			for (Integer iAttempt = 1; iAttempt <= MAXATTEMPTS; iAttempt++) {
				WebTarget target = ucdSession.getUcdWebTarget().path("/cli/resource/deleteResource")
					.queryParam("resource", resource)
					
				Response response = target.request(MediaType.APPLICATION_JSON).delete()
				if (response.getStatus() == 204) {
					logInfo("Resource deleted.")
					break
				} else if (response.getStatus() == 404) {
					logInfo("Resource not found.")
					break
				} else {
					String responseStr = response.readEntity(String.class)
					logInfo(responseStr)
					if (responseStr ==~ /.*bulk manipulation query.*/ && iAttempt < MAXATTEMPTS) {
						logInfo("Attempt $iAttempt failed. Waiting to try again.")
						Thread.sleep(2000)
					} else {
						throw new UcdInvalidValueException(response)
					}
				}
			}
		}
	}	
}
