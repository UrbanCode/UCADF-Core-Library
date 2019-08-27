/**
 * This action updates a generic process.
 */
package org.urbancode.ucadf.core.action.ucd.genericProcess

import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.genericProcess.UcdGenericProcess

import groovy.json.JsonBuilder

class UcdUpdateGenericProcess extends UcAdfAction {
	// Action properties.
	/** The generic process name or ID. */
	String process
	
	/** The name. */
	String name
	
	/** The description. */
	String description
	
	/** The working directory. */
	String workingDir
	
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

		Map requestMap = [
			name: name ?: ucdGenericProcess.getName(),
			description: description ?: ucdGenericProcess.getDescription(),
			defaultResourceId: ucdGenericProcess.getDefaultResourceId(),
			workingDir: workingDir ?: ucdGenericProcess.getWorkingDir(),
			notificationSchemeId: ucdGenericProcess.getNotificationSchemeId(),
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
			logInfo("Generic process [$process] updated.")
		} else {
			throw new UcdInvalidValueException(response)
		}
	}	
}
