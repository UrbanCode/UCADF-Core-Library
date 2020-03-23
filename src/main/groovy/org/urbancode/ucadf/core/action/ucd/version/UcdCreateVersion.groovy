/**
 * This action creates a version.
 */
package org.urbancode.ucadf.core.action.ucd.version

import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucd.version.UcdVersion
import org.urbancode.ucadf.core.model.ucd.version.UcdVersionTypeEnum

class UcdCreateVersion extends UcAdfAction {
	// Action properties.
	/** The component name or ID. */
	String component

	/** The version name. */	
	String name
	
	/** (Optional) The description. */
	String description = ""
	
	/** The version type. Default is FULL. */
	UcdVersionTypeEnum type = UcdVersionTypeEnum.FULL
	
	/** The flag that indicates fail if the version already exists. Default is true. */
	Boolean failIfExists = true
		
	/**
	 * Runs the action.	
	 * @return True if the version was created.
	 */
	@Override
	public Boolean run() {
		// Validate the action properties.
		validatePropsExist()

		Boolean created = false
		
		// Get the existing version information.
		UcdVersion ucdVersion = actionsRunner.runAction([
			action: UcdGetVersion.getSimpleName(),
			actionInfo: false,
			component: component,
			version: name,
			failIfNotFound: false
		])

		// If the version doesn't exist then create it.
		if (ucdVersion) {
			logVerbose("Component [$component] version [$name] already exists.")
			if (failIfExists) {
				throw new UcAdfInvalidValueException("Component [$component] version [$name] already exists.")
			}
		} else {
			logVerbose("Creating version component [$component] version [$name].")
	
			WebTarget target = ucdSession.getUcdWebTarget().path("/cli/version/createVersion")
				.queryParam("component", component)
				.queryParam("name", name)
				.queryParam("description", description)
				.queryParam("type", type)
			logDebug("target=$target")
			
			Response response = target.request(MediaType.APPLICATION_JSON).post(Entity.json(""))
			if (response.getStatus() == 200) {
				ucdVersion = response.readEntity(UcdVersion.class)
				created = true
			} else {
				throw new UcAdfInvalidValueException(response)
			}
			
			logVerbose("Component [$component] version [$name] id [${ucdVersion.getId()}] created.")
			
			return created
		}
	}
}
