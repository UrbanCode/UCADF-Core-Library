/**
 * This action deletes a resource.
 */
package org.urbancode.ucadf.core.action.ucd.resource

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucd.resource.UcdResource

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
			// Attempt to delete the resource. If it encounters a unique result error then try to delete it again with the UUID.
			if (deleteResource(resource)) {
				logError("A unique result error was encountered. Getting parent's child resources to find ID of bad resource.")
				
				String parent, name
				(parent, name) = UcdResource.getParentPathAndName(resource)

				// Get the resource parent's children.
				List<UcdResource> ucdResources = actionsRunner.runAction([
					action: UcdGetChildResources.getSimpleName(),
					actionInfo: false,
					actionVerbose: true,
					resource: parent,
					failIfNotFound: failIfNotFound
				])

				// Look for the resource by name.
				UcdResource ucdResource = ucdResources.find {
					it.getName().equals(name)
				}

				// If the resource was found then try to delete it by ID.
				if (ucdResource) {
					deleteResource(ucdResource.getId())
				}
			}
		}
	}	

	// Attempt to delete the resource.	
	public Boolean deleteResource(final String resource) {
		logVerbose("Deleting Resource [$resource].")
		
		Boolean uniqueResultError = false
		
		// Get the resource to verify it exists and to get the ID.
		WebTarget target = ucdSession.getUcdWebTarget().path("/cli/resource/info")
			.queryParam("resource", resource)
		logDebug("target=$target")
	
		Response response = target.request().get()
		
		if (response.getStatus() == 200) {
			UcdResource ucdResource = response.readEntity(UcdResource)

			// Had to add logic to handle concurrency issue discovered in UCD 6.2.7.0.
			final Integer MAXATTEMPTS = 10
			for (Integer iAttempt = 1; iAttempt <= MAXATTEMPTS; iAttempt++) {
				target = ucdSession.getUcdWebTarget().path("/rest/resource/resource/{resourceId}")
					.resolveTemplate("resourceId", ucdResource.getId())
					
				response = target.request(MediaType.APPLICATION_JSON).delete()
				response.bufferEntity()

				if (response.getStatus() == 204) {
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
							logWarn("Attempt $iAttempt failed. Waiting to try again.")
							Random rand = new Random(System.currentTimeMillis())
							Thread.sleep(rand.nextInt(2000))
						} else {
							throw new UcAdfInvalidValueException(response)
						}
					}
				}
			}
		} else if (response.getStatus() == 400 && response.readEntity(String.class).matches(/.*query did not return a unique result.*/)) {
			uniqueResultError = true
		}
		
		return uniqueResultError
	}
}
