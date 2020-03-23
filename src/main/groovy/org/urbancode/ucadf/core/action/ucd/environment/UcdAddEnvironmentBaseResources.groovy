/**
 * This action adds environment base resources.
 */
package org.urbancode.ucadf.core.action.ucd.environment

import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException

class UcdAddEnvironmentBaseResources extends UcAdfAction {
	/** The application name or ID. */
	String application
	
	/** The environment name or ID. */
	String environment

	/** The list of base resource paths or IDs. */	
	List<String> resources
	
	/**
	 * Runs the action.	
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExist()
		
		for (resource in resources) {
			addEnvironmentBaseResource(resource)
		}
	}
	
	// Add a base resource to an environment.
	public addEnvironmentBaseResource(final String resource) {
		logVerbose("Adding application [$application] environment[$environment] base resource [$resource].")

		WebTarget target = ucdSession.getUcdWebTarget().path("/cli/environment/addBaseResource")
			.queryParam("application", application)
			.queryParam("environment", environment)
			.queryParam("resource", resource)
		logDebug("target=$target")
			
		Response response = target.request(MediaType.APPLICATION_JSON).put(Entity.json(""))
		if (response.getStatus() != 204) {
            throw new UcAdfInvalidValueException(response)
		}

		logVerbose("Base resource added to environment.")
	}
}
