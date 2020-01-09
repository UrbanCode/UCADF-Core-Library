/**
 * This action creates a generic process.
 */
package org.urbancode.ucadf.core.action.ucd.genericProcess

import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.action.ucd.resource.UcdGetResource
import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.general.UcdObject
import org.urbancode.ucadf.core.model.ucd.genericProcess.UcdGenericProcess
import org.urbancode.ucadf.core.model.ucd.resource.UcdResource

import groovy.json.JsonBuilder

class UcdCreateGenericProcess extends UcAdfAction {
	// Action properties.
	/** The generic process name. */
	String name
	
	/** (Optional) The description. (Optional) */
	String description = ""
	
	/** The working directory. (Optional) */
	String workingDir = UcdGenericProcess.WORKINGDIRECTORY_DEFAULT

	/** The default resource. (Optional) */
	String defaultResource = ""

	/** The notification scheme. (Optional) */	
	String notificationScheme = ""
	
	/** The flag that indicates fail if the generic process already exists. Default is true. */
	Boolean failIfExists = true

	/**
	 * Runs the action.	
	 * @return True if the generic process was created.
	 */
	@Override
	public Boolean run() {
		// Validate the action properties.
		validatePropsExistInclude(
			[
				"name"
			]
		)

		Boolean created = false
		
		logInfo("Creating generic process [$name].")
		
		// If an defaultResource ID was provided then use it. Otherwise get the defaultResource information to get the ID.
		String defaultResourceId = defaultResource
		if (defaultResource) {
			if (!UcdObject.isUUID(defaultResource)) {
				UcdResource ucdResource = actionsRunner.runAction([
					action: UcdGetResource.getSimpleName(),
					actionInfo: false,
					resource: defaultResource,
					failIfNotFound: true
				])
				defaultResourceId = ucdResource.getId()
			}
		}

		// If an defaultResource ID was provided then use it. Otherwise get the defaultResource information to get the ID.
		String notificationSchemeId = notificationScheme
		if (notificationScheme) {
			if (!UcdObject.isUUID(notificationScheme)) {
				// TODO: Need to have notification scheme lookup.
				throw new UcdInvalidValueException("Ability to specify notification scheme name not implemented yet.")
			}
		}

		Map requestMap = [
			name: name,
			description: description,
			defaultResourceId: defaultResourceId,
			workingDir: workingDir,
			notificationSchemeId: notificationSchemeId,
			properties: [
				workingDir: workingDir
			],
			teamMappings: []
		]
		
		JsonBuilder jsonBuilder = new JsonBuilder(requestMap)
		logDebug("jsonBuilder=$jsonBuilder")

		// Create the generic process.
		WebTarget target = ucdSession.getUcdWebTarget().path("/rest/process")
        logDebug("target=$target")

        Response response = target.request(MediaType.APPLICATION_JSON).put(Entity.json(jsonBuilder.toString()))
        if (response.getStatus() == 200) {
			logInfo("Generic process [$name] created.")
			created = true
        } else {
			String errMsg = UcdInvalidValueException.getResponseErrorMessage(response)
			logInfo(errMsg)
			if (response.getStatus() != 400 || failIfExists) {
				throw new UcdInvalidValueException(errMsg)
			}
        }
		
		return created
	}
}
