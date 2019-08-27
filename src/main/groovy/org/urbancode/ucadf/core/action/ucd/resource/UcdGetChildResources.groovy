/**
 * This action returns a list of child resources.
 */
package org.urbancode.ucadf.core.action.ucd.resource

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.GenericType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.environment.UcdEnvironment
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.general.UcdObject
import org.urbancode.ucadf.core.model.ucd.resource.UcdResource

class UcdGetChildResources extends UcAdfAction {
	/** The resource path or ID. */
	String resource
	
	/** (Optional) The tag to find a resource. */
	String tag = ""
	
	/** The flag that indicates fail if the parent resource is not found. Default is true. */
	Boolean failIfNotFound = true
	
	/**
	 * Runs the action.	
	 * @return The list of resource objects.
	 */
	@Override
	public List<UcdResource> run() {
		// Validate the action properties.
		validatePropsExist()

		// Gets information about all resources under a given parent, optionally including the resource properties.
		List<UcdResource> ucdResources = []
		
		WebTarget target = ucdSession.getUcdWebTarget().path("/cli/resource")

		// Don't add parent paremeter if getting top-level resource.
		if (!("".equals(resource) || "/".equals(resource))) {
			target = target.queryParam("parent", resource)
		}
		
		logDebug("target=$target")
		
		Response response = target.request().get()
		if (response.getStatus() == 200) {
			// Get the list of objects.
			List<UcdResource> returnedUcdResources = response.readEntity(new GenericType<List<UcdResource>>(){})

			// For some reason the UCD API returns multiple entries of the same resource if it has multiple tags.
			// We only want to return one entry per resource so we skip the extra ones.
			for (UcdResource returnedUcdResource in returnedUcdResources) {
				if (!ucdResources.find { it.getId() == returnedUcdResource.getId()}) {
					if (!tag || returnedUcdResource.hasTag(tag)) {
						ucdResources.add(returnedUcdResource)
					}
				}
			}
		} else {
			String errMsg = UcdInvalidValueException.getResponseErrorMessage(response)
			logInfo(errMsg)
			if (response.getStatus() != 404 || failIfNotFound) {
				throw new UcdInvalidValueException(errMsg)
			}
		}

		return ucdResources
	}
}
