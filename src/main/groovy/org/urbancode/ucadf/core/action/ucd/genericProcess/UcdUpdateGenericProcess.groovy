/**
 * This action updates a generic process.
 */
package org.urbancode.ucadf.core.action.ucd.genericProcess

import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.action.ucd.resource.UcdGetResource
import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucd.general.UcdObject
import org.urbancode.ucadf.core.model.ucd.genericProcess.UcdGenericProcess
import org.urbancode.ucadf.core.model.ucd.resource.UcdResource

import groovy.json.JsonBuilder

class UcdUpdateGenericProcess extends UcAdfAction {
	// Action properties.
	/** The generic process name or ID. */
	String process
	
	/** The new name for the process (Optional). */
	String name
	
	/** The new description. (Optional) */
	String description
	
	/** The working directory. (Optional) */
	String workingDir

	/** The default resource. (Optional) */
	String defaultResource

	/** The notification scheme. (Optional) */	
	String notificationScheme
	
	/**
	 * Runs the action.	
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExistInclude(
			[
				"process"
			]
		)

		// Get the generic process information.
		UcdGenericProcess ucdGenericProcess = actionsRunner.runAction([
			action: UcdGetGenericProcess.getSimpleName(),
			process: process,
			failIfNotFound: true
		])

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
				throw new UcAdfInvalidValueException("Ability to specify notification scheme name not implemented yet.")
			}
		}

		Map requestMap = [
			name: name ?: ucdGenericProcess.getName(),
			description: (description != null) ?: ucdGenericProcess.getDescription(),
			defaultResourceId: defaultResourceId ?: ucdGenericProcess.getDefaultResourceId(),
			workingDir: workingDir ?: ucdGenericProcess.getWorkingDir(),
			notificationSchemeId: notificationSchemeId ?: ucdGenericProcess.getNotificationSchemeId(),
			existingId: ucdGenericProcess.getId(),
			processVersion: ucdGenericProcess.getVersion(),
			properties: [
				workingDir: workingDir ?: ucdGenericProcess.getWorkingDir()
			]
		]

		JsonBuilder jsonBuilder = new JsonBuilder(requestMap)

		WebTarget target = ucdSession.getUcdWebTarget().path("/rest/process")
		logDebug("target=$target")
		
		Response response = target.request(MediaType.APPLICATION_JSON).put(Entity.json(jsonBuilder.toString()))
		if (response.getStatus() == 200) {
			logVerbose("Generic process [$process] updated.")
		} else {
			throw new UcAdfInvalidValueException(response)
		}
	}	
}
