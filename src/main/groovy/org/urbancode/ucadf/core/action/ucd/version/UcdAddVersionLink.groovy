/**
 * This action adds version links
 */
package org.urbancode.ucadf.core.action.ucd.version

import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucd.version.UcdVersion

class UcdAddVersionLink extends UcAdfAction {
	// Action properties.
	/** The component name or ID. */
	String component

	/** The version name or ID. */	
	String version
	
	/** The link name. */
	String name
	
	/** The link value. */
	String value
	
	/**
	 * Runs the action.	
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExist()
		
        logVerbose("Add component [$component] version [$version] link name [$name] value [$value] in instance [${ucdSession.getUcdUrl().toString()}]")

		// Work around 7.0 bug where it converts a version name with 4 hyphens to a UUID.
		if (isIncorrectlyInterpretedAsUUID(version)) {
			UcdVersion ucdVersion = actionsRunner.runAction([
				action: UcdGetVersion.getSimpleName(),
				actionInfo: false,
				actionVerbose: false,
				component: component,
				version: version,
				failIfNotFound: true
			])
			
			version = ucdVersion.getId()
		}
		
        WebTarget target = ucdSession.getUcdWebTarget().path("/cli/version/addLink")
	        .queryParam("component", component)
	        .queryParam("version", version)
	        .queryParam("linkName", name)
			.queryParam("link", value)
		logDebug("target=$target")
		
        Response response = target.request(MediaType.APPLICATION_JSON).put(Entity.json(""))
        if (response.getStatus() == 204) {
            logVerbose("Version link added.")
        } else {
            throw new UcAdfInvalidValueException(response)
        }
    }
}
