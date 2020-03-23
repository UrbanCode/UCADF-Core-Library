/**
 * This action deletes a version.
 */
package org.urbancode.ucadf.core.action.ucd.version

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucd.general.UcdObject
import org.urbancode.ucadf.core.model.ucd.version.UcdVersion

class UcdDeleteVersion extends UcAdfAction {
	// Action properties.
	/** The component name or ID. */
	String component
	
	/** The version name or ID. */
	String version
	
	/** The flag that indicates to perform the delete, otherwise show that the delete would be done. Default is true. */
	Boolean commit = true
	
	/** The flag that indicates fail if the version is not found. Default is false. */
	Boolean failIfNotFound = false
		
	/**
	 * Runs the action.	
	 * @return True if the version was deleted.
	 */
	@Override
	public Boolean run() {
		// Validate the action properties.
		validatePropsExist()
		
		Boolean deleted = false
		
		// If an version ID was provided then use it. Otherwise get the version information to get the ID.
		String versionId
		if (UcdObject.isUUID(version)) {
			versionId = version
		} else {
			UcdVersion ucdVersion = actionsRunner.runAction([
				action: UcdGetVersion.getSimpleName(),
				component: component,
				version: version,
				failIfNotFound: failIfNotFound
			])
			
			if (ucdVersion) {
				versionId = ucdVersion.getId()
			} else {
				String errMsg = "Version [$version] not found."
				logVerbose(errMsg)
				if (failIfNotFound) {
					throw new UcAdfInvalidValueException(errMsg)
				}
			}
		}

		if (versionId) {
			if (commit) {
				WebTarget target = ucdSession.getUcdWebTarget().path("/rest/deploy/version/{versionId}")
					.resolveTemplate("versionId", versionId)
				logDebug("target=$target")
				
				Response response = target.request(MediaType.APPLICATION_JSON).delete()
				if (response.getStatus() == 200) {
					logVerbose("Component [$component] Version [$version] deleted.")
					deleted = true
				} else {
					String errMsg = UcAdfInvalidValueException.getResponseErrorMessage(response)
					logVerbose(errMsg)
					if (response.getStatus() != 404 || failIfNotFound) {
						throw new UcAdfInvalidValueException(errMsg)
					}
				}
			} else {
				logVerbose("Would delete component [$component] version [$version]")
			}
		}
		
		return deleted
	}
}
