/**
 * This action removes a resource's component version inventory.
 */
package org.urbancode.ucadf.core.action.ucd.resource

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException

class UcdRemoveResourceInventory extends UcAdfAction {
	// Action properties.
	/** The resource path or ID. */
	String resource
	
	/** The component name or ID. */
	String component

	/** The version. */	
	String version
	
	/** (Optional) The status. */
	String status = ""
	
	/** The flag that indicates fail if the resource inventory is not found. Default is false. */
	Boolean failIfNotFound = false
	
	/**
	 * Runs the action.	
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExist()
		
		logVerbose("Delete resource [$resource] inventory for component [$component] version [$version].")

		WebTarget target = ucdSession.getUcdWebTarget().path("/rest/inventory/resourceInventoryForComponent/")
			.queryParam("resource", resource)
			.queryParam("component", component)
			.queryParam("version", version)
			.queryParam("status", status)
		logDebug("target=$target")
		
		Response response = target.request(MediaType.APPLICATION_JSON).delete()
		if (response.getStatus() == 204) {
			logVerbose("Resource inventory deleted.")
		} else if (response.getStatus() != 404 || failIfNotFound) {
			throw new UcAdfInvalidValueException(response)
		}
	}
}
