/**
 * This action deletes a resource property.
 */
package org.urbancode.ucadf.core.action.ucd.resource

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException

class UcdDeleteResourceProperty extends UcAdfAction {
	/** The resource path or ID. */
	String resource
	
	/** The property name or ID. */
	String property
	
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

		logVerbose("Delete resource [$resource] property [$property] commit [$commit].")
		if (!commit) {
			logVerbose("Would delete resource [$resource] property [$property].")
		} else {
			WebTarget target = ucdSession.getUcdWebTarget().path("/cli/resource/deleteProperty")
				.queryParam("resource", resource)
				.queryParam("name", property)
			Response response = target.request(MediaType.APPLICATION_JSON).delete()
			if (response.getStatus() == 204) {
				logVerbose("Resource [$resource] property [$property] deleted.")
			} else if (response.getStatus() != 400 || failIfNotFound) {
				throw new UcAdfInvalidValueException(response)
			}
		}
	}
}
