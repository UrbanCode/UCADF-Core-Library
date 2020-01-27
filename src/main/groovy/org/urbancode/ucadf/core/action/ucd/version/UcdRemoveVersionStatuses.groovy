/**
 * This action removes version statuses.
 */
package org.urbancode.ucadf.core.action.ucd.version

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.version.UcdVersion

class UcdRemoveVersionStatuses extends UcAdfAction {
	// Action properties.
	/** The component name or ID. */
	String component
	
	/** The version name or ID. */
	String version
	
	/** The list of status names or IDs. */
	List<String> statuses
	
	/**
	 * Runs the action.	
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExist()
		
		// Work around 7.0 bug where it converts a version name with 4 hyphens to a UUID.
		if (isIncorrectlyInterpretedAsUUID(version)) {
			UcdVersion ucdVersion = actionsRunner.runAction([
				action: UcdGetVersion.getSimpleName(),
				actionInfo: false,
				component: component,
				version: version,
				failIfNotFound: true
			])
			
			version = ucdVersion.getId()
		}
		
		for (status in statuses) {
			logVerbose("Removing status [$status] to component [$component] version [$version]")

			WebTarget target = ucdSession.getUcdWebTarget().path("/cli/version/status")
				.queryParam("component", component)
				.queryParam("version", version)
				.queryParam("status", status)
			logDebug("target=$target")
			
			Response response = target.request(MediaType.APPLICATION_JSON).delete()
			if (response.getStatus() == 204) {
				logVerbose("Status [$status] removed from component [$component] version [$version].")
			} else {
				throw new UcdInvalidValueException(response)
			}
		}
	}
}
