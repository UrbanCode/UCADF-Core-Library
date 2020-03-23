/**
 * This action deletes a resource.
 */
package org.urbancode.ucadf.core.action.ucd.resource

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException

class UcdDeleteResource extends UcAdfAction {
	// Action properties.
	/** The resource path or ID. */
	String resource
	
	/** The flag that indicates to perform the delete, otherwise show that the delete would be done. Default is true. */
	Boolean commit = true
	
	/** The flag that indicates fail if the resource property is not found. Default is false. */
	Boolean failIfNotFound = false

	/**
	 * Runs the action.	
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExist()
		
		if (!commit) {
			logVerbose("Would delete resource [$resource] from ucdUrl [${ucdSession.getUcdUrl()}].")
		} else {
			logVerbose("Deleting Resource [$resource].")

			// Had to add logic to handle concurrency issue discovered in UCD 6.2.7.0.
			final Integer MAXATTEMPTS = 5
			for (Integer iAttempt = 1; iAttempt <= MAXATTEMPTS; iAttempt++) {
				WebTarget target = ucdSession.getUcdWebTarget().path("/cli/resource/deleteResource")
					.queryParam("resource", resource)
					
				Response response = target.request(MediaType.APPLICATION_JSON).delete()
				if (response.getStatus() == 204) {
					logVerbose("Resource deleted.")
					break
				} else {
					String responseStr = response.readEntity(String.class)
					logVerbose(responseStr)
					if (response.getStatus() == 404) {
						if (failIfNotFound) {
							throw new UcAdfInvalidValueException(response)
						}
						break
					} else {
						if (responseStr ==~ /.*bulk manipulation query.*/ && iAttempt < MAXATTEMPTS) {
							logVerbose("Attempt $iAttempt failed. Waiting to try again.")
							Thread.sleep(2000)
						} else {
							throw new UcAdfInvalidValueException(response)
						}
					}
				}
			}
		}
	}	
}
