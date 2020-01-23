/**
 * This action deletes a component.
 */
package org.urbancode.ucadf.core.action.ucd.component

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException

class UcdDeleteComponent extends UcAdfAction {
	// Action properties.
	/** The component name or ID. */
	String component
	
	/** The flag that indicates fail if the component is not found. Default is false. */
	Boolean failIfNotFound = false
	
	/** The flag that indicates to perform the delete, otherwise show that the delete would be done. Default is true. */
	Boolean commit = true

	/**
	 * Runs the action.	
	 * @return True if the component was deleted.
	 */
	@Override
	public Boolean run() {
		// Validate the action properties.
		validatePropsExist()

		Boolean deleted = false
				
		logVerbose("Deleting component [$component] commit [$commit].")

		if (commit) {
			WebTarget target = ucdSession.getUcdWebTarget().path("/rest/deploy/component/{component}")
				.resolveTemplate("component", component)
			logDebug("target=$target")
			
			Response response = target.request(MediaType.APPLICATION_JSON).delete()
			if (response.getStatus() == 204) {
				logVerbose("Component [$component] deleted.")
				deleted = true
			} else {
				String errMsg = UcdInvalidValueException.getResponseErrorMessage(response)
				logVerbose(errMsg)
				if (response.getStatus() != 404 || failIfNotFound) {
					throw new UcdInvalidValueException(errMsg)
				}
			}
		} else {
			logVerbose("Would delete component [$component].")
		}
		
		return deleted
	}
}
