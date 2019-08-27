/**
 * This action returns an environment's base resources.
 */
package org.urbancode.ucadf.core.action.ucd.environment

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.GenericType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.resource.UcdResource

class UcdGetEnvironmentBaseResources extends UcAdfAction {
	/** The application name or ID. */
	String application
	
	/** The environment name or ID. */
	String environment
	
	/**
	 * Runs the action.	
	 * @return The list of base resources.
	 */
	@Override
	public List<UcdResource> run() {
		// Validate the action properties.
		validatePropsExist()
		
		List<UcdResource> ucdResources = []
		
		WebTarget target = ucdSession.getUcdWebTarget().path("/cli/environment/getBaseResources")
			.queryParam("environment", environment)
			.queryParam("application", application)
		logDebug("target=$target")
		
		Response response = target.request().get()
		if (response.getStatus() == 200) {
			ucdResources = response.readEntity(new GenericType<List<UcdResource>>(){})
		} else if (response.getStatus() != 404) {
            throw new UcdInvalidValueException(response)
		}
		
		return ucdResources
	}
}
