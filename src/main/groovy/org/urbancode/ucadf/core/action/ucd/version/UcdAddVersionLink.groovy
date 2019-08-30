/**
 * This action adds version links
 */
package org.urbancode.ucadf.core.action.ucd.version

import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException

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
		
        logInfo("Add component [$component] version [$version] link name [$name] value [$value] in instance [${ucdSession.getUcdUrl().toString()}]")

        WebTarget target = ucdSession.getUcdWebTarget().path("/cli/version/addLink")
	        .queryParam("component", component)
	        .queryParam("version", version)
	        .queryParam("linkName", name)
			.queryParam("link", value)
		logDebug("target=$target")
		
        Response response = target.request(MediaType.APPLICATION_JSON).put(Entity.json(""))
        if (response.getStatus() == 204) {
            logInfo("Version link added.")
        } else {
            throw new UcdInvalidValueException(response)
        }
    }
}
