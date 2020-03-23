/**
 * This action gets a component version.
 */
package org.urbancode.ucadf.core.action.ucd.version

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucd.general.UcdObject
import org.urbancode.ucadf.core.model.ucd.version.UcdVersion

class UcdGetVersion extends UcAdfAction {
	// Action properties.
	/** The component name or ID. */
	String component
	
	/** The version name or ID. */
	String version
	
	/** The flag that indicates fail if the version is not found. Default is true. */
	Boolean failIfNotFound = true
	
	/**
	 * Runs the action.	
	 * @return The component version object.
	 */
	@Override
	public UcdVersion run() {
		// Validate the action properties.
		validatePropsExistExclude([ 'component' ])

		UcdVersion ucdVersion

		if (component) {
			logVerbose("Getting component [$component] version [$version].")
		} else {
			logVerbose("Getting version [$version].")
		}

		WebTarget target
		Response response

		String versionId = version

		if (!UcdObject.isUUID(version)) {
			if (!component) {
				throw new UcAdfInvalidValueException("A component name must be specified if the version specified is not a UUID.")
			}
			
			// Work around 7.0 bug where it converts a version name with 4 hyphens to a UUID.
			if (isIncorrectlyInterpretedAsUUID(version)) {
				// Look for a component version that matches the name.
				List<UcdVersion> ucdVersions = actionsRunner.runAction([
					action: UcdGetComponentVersions.getSimpleName(),
					actionInfo: false,
					component: component,
					match: /^$version$/,
					numResults: 1,
					failIfNotFound: true
				])
				
				versionId = ucdVersions[0].getId()
			} else {
				// Look up the version ID.		
				target = ucdSession.getUcdWebTarget().path("/cli/version/getVersionId")
					.queryParam("component", component)
					.queryParam("version", version)
				logDebug("target=$target")

				response = target.request().get()
				if (response.getStatus() == 200) {
					versionId = response.readEntity(String.class)
				} else {
					String errMsg = UcAdfInvalidValueException.getResponseErrorMessage(response)
					logVerbose(errMsg)
					if (response.getStatus() != 400 || (!errMsg ==~ /.*could not be resolved.*/) || failIfNotFound) {
						throw new UcAdfInvalidValueException(errMsg)
					}
				}
			}
		}

		// Get the version object.		
		target = ucdSession.getUcdWebTarget().path("/rest/deploy/version/{versionId}")
			.resolveTemplate("versionId", versionId)
		logDebug("target=$target")
		
		response = target.request().get()
		if (response.getStatus() == 200) {
			ucdVersion = response.readEntity(UcdVersion.class)
		} else {
			String errMsg = UcAdfInvalidValueException.getResponseErrorMessage(response)
			logVerbose(errMsg)
			if (response.getStatus() != 404 || failIfNotFound) {
				throw new UcAdfInvalidValueException(errMsg)
			}
		}

		return ucdVersion
	}
}
