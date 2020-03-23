/**
 * This action gets a version's properties.
 */
package org.urbancode.ucadf.core.action.ucd.version

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.GenericType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucd.property.UcdProperty
import org.urbancode.ucadf.core.model.ucd.version.UcdVersion

class UcdGetVersionProperties extends UcAdfAction {
	// Action properties.
	/** The component name or ID. */
	String component
	
	/** The version name or ID. */
	String version
	
	/** The flag that indicates fail if the version is not found. Default is true. */
	Boolean failIfNotFound = true
	
	/**
	 * Runs the action.	
	 * @return The map of the version properties.
	 */
	@Override
	public Map <String, UcdProperty> run() {
		// Validate the action properties.
		validatePropsExist()

		Map <String, UcdProperty> propertiesMap = [:]
		
		logVerbose("Getting component [$component] version [$version] properties.")

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

		WebTarget target = ucdSession.getUcdWebTarget().path("/cli/version/versionProperties")
			.queryParam("component", component)
			.queryParam("version", version)
		logDebug("target=$target")

		Response response = target.request().get()
		if (response.getStatus() == 200) {
			List<UcdProperty> ucdProperties = response.readEntity(new GenericType<List<UcdProperty>>(){})
			propertiesMap = ucdProperties.collectEntries { [ (it.getName()): it ] }
		} else {
			if (response.getStatus() != 404 || failIfNotFound) {
				throw new UcAdfInvalidValueException(response)
			}
		}
		
		return propertiesMap
	}
}
