/**
 * This action gets a component's latest version.
 */
package org.urbancode.ucadf.core.action.ucd.version

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucd.version.UcdVersion

class UcdGetComponentLatestVersion extends UcAdfAction {
	// Action properties.
	/** The component name or ID. */
	String component
	
	/** The flag that indicates fail if the component latest version is not found. Default is true. */
	Boolean failIfNotFound = true
	
	/**
	 * Runs the action.	
	 * @return The latest version object.
	 */
	@Override
	public UcdVersion run() {
		// Validate the action properties.
		validatePropsExist()
		
		UcdVersion ucdVersion
		
		WebTarget target = ucdSession.getUcdWebTarget().path("/rest/deploy/component/{component}/latestVersion")
			.resolveTemplate("component", component)
		logDebug("target=$target")
		
		Response response = target.request().get()
		if (response.getStatus() == 200) {
			ucdVersion = response.readEntity(UcdVersion.class)
			logVerbose("Component [$component] latest version [${ucdVersion.getName()}].")
		} else {
			String errMsg = UcAdfInvalidValueException.getResponseErrorMessage(response)
			logVerbose(errMsg)
			if (response.getStatus() == 204 || response.getStatus() == 404) {
				if (failIfNotFound) {
					throw new UcAdfInvalidValueException(errMsg)
				}
			} else {
				throw new UcAdfInvalidValueException(errMsg)
			}
		}
		
		if (ucdVersion) {
			outProps.put("ucAdfVersionId", ucdVersion.getId())
			outProps.put("ucAdfVersionName", ucdVersion.getName())
			outProps.put("ucAdfHadLatestVersion", true)
		} else {
			outProps.put("ucAdfHadLatestVersion", false)
		}
		
		return ucdVersion
	}
}
