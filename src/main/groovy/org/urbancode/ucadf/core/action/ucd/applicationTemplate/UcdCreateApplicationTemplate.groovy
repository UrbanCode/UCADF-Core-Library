/**
 * This action creates a application template.
 */
package org.urbancode.ucadf.core.action.ucd.applicationTemplate

import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.action.ucd.notificationScheme.UcdGetNotificationScheme
import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucd.applicationTemplate.UcdApplicationTemplateTagRequirementRequest
import org.urbancode.ucadf.core.model.ucd.general.UcdObject
import org.urbancode.ucadf.core.model.ucd.notificationScheme.UcdNotificationScheme

import groovy.json.JsonBuilder

class UcdCreateApplicationTemplate extends UcAdfAction {
	// Action properties.
	/** The name. */
	String name

	/** (Optional) The description. */
	String description = ""
	
	/** The flag that indicates fail if the application template exists. Default is true. */
	Boolean failIfExists = true

	/** The notification scheme. (Optional) */	
	String notificationScheme = ""
	
	/** The flat that indicates to enforce complete snapshots. Default is false. */
	Boolean enforceCompleteSnapshots = false
	
	/** The tag requirements */
	List<UcdApplicationTemplateTagRequirementRequest> tagRequirements = []
	
	/**
	 * Runs the action.	
	 * @return True if the application template was created.
	 */
	@Override
	public Boolean run() {
		// Validate the action properties.
		validatePropsExist()

		Boolean created = false
		
		logVerbose("Creating application template [$name].")
		
		// If notification scheme ID was provided then use it. Otherwise, look up the ID.
		String notificationSchemeId = notificationScheme
		if (notificationScheme) {
			if (!UcdObject.isUUID(notificationScheme)) {
				UcdNotificationScheme ucdNotificationScheme = actionsRunner.runAction([
					action: UcdGetNotificationScheme.getSimpleName(),
					actionInfo: false,
					notificationScheme: notificationScheme,
					failIfNotFound: true
				])
				notificationSchemeId = ucdNotificationScheme.getId()
			}
		}

		Map requestMap = [
			name: name,
			description: description,
			notificationSchemeId: notificationSchemeId,
			enforceCompleteSnapshots: enforceCompleteSnapshots,
			tagRequirements: tagRequirements,
			teamMappings: []
		]

		JsonBuilder jsonBuilder = new JsonBuilder(requestMap)
		logDebug("jsonBuilder=$jsonBuilder")
			
		// Create the application template.
		WebTarget target = ucdSession.getUcdWebTarget().path("/rest/deploy/applicationTemplate")
        logDebug("target=$target")

        Response response = target.request(MediaType.APPLICATION_JSON).put(Entity.json(jsonBuilder.toString()))
        if (response.getStatus() == 200) {
			logVerbose("Application template [$name] created.")
			created = true
        } else {
			String errMsg = UcAdfInvalidValueException.getResponseErrorMessage(response)
			logVerbose(errMsg)
			if (!(response.getStatus() == 400 && (errMsg ==~ /.*already exists.*/ && !failIfExists))) {
				throw new UcAdfInvalidValueException(errMsg)
			}
        }
		
		return created
	}
}
