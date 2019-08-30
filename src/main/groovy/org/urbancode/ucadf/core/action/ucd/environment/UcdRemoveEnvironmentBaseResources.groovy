/**
 * This action removes environment base resources.
 */
package org.urbancode.ucadf.core.action.ucd.environment

import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.resource.UcdResource

class UcdRemoveEnvironmentBaseResources extends UcAdfAction {
	/** The application name or ID. */
	String application
	
	/** The environment name or ID. */
	String environment

	/** The list of base resource paths or IDs. */	
	List<String> resources = []
	
	/** (Optional) The base resource pattern to match. */
	String matchPath = ""
	
	/**
	 * Runs the action.	
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExist()

		// If an array of resources was provided then remove those.
		if (resources.size() > 0) {		
			for (resource in resources) {
				removeEnvironmentBaseResource(resource)
			}
		}
		
		// If a match value was provided then get the base resources and remove the matching ones.
		if (matchPath) {
			List<UcdResource> ucdResources = actionsRunner.runAction([
				action: UcdGetEnvironmentBaseResources.getSimpleName(),
				application: application,
				environment: environment
			])
			
			for (ucdResource in ucdResources) {
				if (ucdResource.getPath() ==~ /$matchPath/) {
					removeEnvironmentBaseResource(ucdResource.getPath())
				}
			}
		}
	}
	
	// Remove a base resource from an environment.
	public Response removeEnvironmentBaseResource(String resource) {
		logInfo("Removing base resource [$resource] from application [$application] environment [$environment].")
			
		WebTarget target = ucdSession.getUcdWebTarget().path("/cli/environment/removeBaseResource")
			.queryParam("application", application)
			.queryParam("environment", environment)
			.queryParam("resource", resource)
		logDebug("target=$target")
		
		Response response = target.request(MediaType.APPLICATION_JSON).put(Entity.json(""))
		if (response.getStatus() == 204) {
			logInfo("Base resource [$resource] removed from application [$application] environment [$environment].")
		} else if (response.getStatus() == 404) {
			logInfo("Base resource [$resource] not not found in application [$application] environment [$environment].")
		} else {
            throw new UcdInvalidValueException(response)
		}
		
		return response
	}
}
