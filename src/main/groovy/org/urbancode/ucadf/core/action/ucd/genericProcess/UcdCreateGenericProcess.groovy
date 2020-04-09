/**
 * This action creates a generic process.
 */
package org.urbancode.ucadf.core.action.ucd.genericProcess

import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.action.ucd.notificationScheme.UcdGetNotificationScheme
import org.urbancode.ucadf.core.action.ucd.resource.UcdGetResource
import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucd.general.UcdObject
import org.urbancode.ucadf.core.model.ucd.genericProcess.UcdGenericProcess
import org.urbancode.ucadf.core.model.ucd.notificationScheme.UcdNotificationScheme
import org.urbancode.ucadf.core.model.ucd.resource.UcdResource
import org.urbancode.ucadf.core.model.ucd.system.UcdSession

import groovy.json.JsonBuilder

class UcdCreateGenericProcess extends UcAdfAction {
	// Action properties.
	/** The generic process name. */
	String name
	
	/** (Optional) The description. (Optional) */
	String description = ""
	
	/** The working directory. (Optional) */
	String workingDir = UcdGenericProcess.WORKINGDIRECTORY_DEFAULT

	/** The default resource. (Optional. Deprecated in 7.0.4) */
	String defaultResource = ""

	/** The default resources. (Optional. Added in 7.0.4) */
	List<String> defaultResources = []

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
		
		logVerbose("Creating generic process [$name].")

		// If a single resource was specified then add it to the list.
		if (defaultResource) {
			defaultResources.add(defaultResource)
		}
		
		List<String> defaultResourceIds = []
				
		// If defaultResource IDs were provided then use then. Otherwise get the resource information to get the ID.
		for (defaultResourceItem in defaultResources) {
			String defaultResourceId = defaultResourceItem
			if (defaultResource) {
				if (!UcdObject.isUUID(defaultResource)) {
					UcdResource ucdResource = actionsRunner.runAction([
						action: UcdGetResource.getSimpleName(),
						actionInfo: false,
						resource: defaultResourceItem,
						failIfNotFound: true
					])
					defaultResourceId = ucdResource.getId()
				}
			}
			defaultResourceIds.add(defaultResourceId)
		}
		
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
			workingDir: workingDir,
			notificationSchemeId: notificationSchemeId,
			properties: [
				workingDir: workingDir
			],
			teamMappings: []
		]

		if (ucdSession.compareVersion(UcdSession.UCDVERSION_704) >= 0) {
			requestMap.put('defaultResourceIds', defaultResourceIds)
		} else {
			requestMap.put(
				'defaultResourceId', 
				(defaultResourceIds.size() > 0 ? defaultResourceIds[0] : "")
			)
		}

		JsonBuilder jsonBuilder = new JsonBuilder(requestMap)
		logDebug("jsonBuilder=$jsonBuilder")

		// Create the generic process.
		WebTarget target = ucdSession.getUcdWebTarget().path("/rest/process")
        logDebug("target=$target")

        Response response = target.request(MediaType.APPLICATION_JSON).put(Entity.json(jsonBuilder.toString()))
        if (response.getStatus() == 200) {
			logVerbose("Generic process [$name] created.")
			created = true
        } else {
			String errMsg = UcAdfInvalidValueException.getResponseErrorMessage(response)
			logVerbose(errMsg)
			if (response.getStatus() != 400 || failIfExists) {
				throw new UcAdfInvalidValueException(errMsg)
			}
        }
		
		return created
	}
}
