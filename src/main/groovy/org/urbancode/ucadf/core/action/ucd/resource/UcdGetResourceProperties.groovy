/**
 * This action returns a list of resource property objects.
 */
package org.urbancode.ucadf.core.action.ucd.resource

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.GenericType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.property.UcdProperty

class UcdGetResourceProperties extends UcAdfAction {
	// Action properties.
	/** The resource path or ID. */
	String resource
	
	/** The flag that indicates fail if the resource is not found. Default is true. */
	Boolean failIfNotFound = true
	
	/**
	 * Runs the action.	
	 * @return The list of resource property objects.
	 */
	@Override
	public List<UcdProperty> run() {
		// Validate the action properties.
		validatePropsExist()

		List<UcdProperty> ucdProperties = []
		
		logInfo("Getting resource [$resource] properties.")

		WebTarget target = ucdSession.getUcdWebTarget().path("/cli/resource/getProperties")
			.queryParam("resource", resource)
		logDebug("target=$target")

		Response response = target.request().get()
		if (response.getStatus() == 200) {
			ucdProperties = response.readEntity(new GenericType<List<UcdProperty>>(){})
		} else {
			if (response.getStatus() != 404 || failIfNotFound) {
				throw new UcdInvalidValueException(response)
			}
		}
		
		return ucdProperties
	}
}
