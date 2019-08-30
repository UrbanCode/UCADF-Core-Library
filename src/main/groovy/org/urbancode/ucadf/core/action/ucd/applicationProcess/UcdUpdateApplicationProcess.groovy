/**
 * This action updates an application process.
 */
package org.urbancode.ucadf.core.action.ucd.applicationProcess

import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.action.ucd.role.UcdGetRole
import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.applicationProcess.UcdApplicationProcess
import org.urbancode.ucadf.core.model.ucd.applicationProcess.UcdApplicationProcessInventoryManagementTypeEnum
import org.urbancode.ucadf.core.model.ucd.applicationProcess.UcdApplicationProcessOfflineAgentHandlingEnum
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.general.UcdObject
import org.urbancode.ucadf.core.model.ucd.role.UcdRole

import groovy.json.JsonBuilder

class UcdUpdateApplicationProcess extends UcAdfAction {
	// Action properties.
	/** The application name or ID. */
	String application
	
	/** The process name or ID. */
	String process
	
	// Optional action properties.
	/** The application process description. */
	String description
	
	/** The inventory management type. */
	UcdApplicationProcessInventoryManagementTypeEnum inventoryManagementType
	
	/** The offlien agent handling. */
	UcdApplicationProcessOfflineAgentHandlingEnum offlineAgentHandling
	
	/** The required role. */
	String requiredRole
	
	/**
	 * Runs the action.	
	 */
	@Override
	public Object run() {
		// Validate the action properties with some excluded.
		validatePropsExistExclude([
			"description",
			"inventoryManagementType",
			"offlineAgentHandling",
			"requiredRole"
		])

		logInfo("Updating application [$application] process [$process].")
	
		UcdApplicationProcess ucdApplicationProcess = actionsRunner.runAction([
			action: UcdGetApplicationProcess.getSimpleName(),
			actionInfo: false,
			application: application,
			process: process,
			failIfNotFound: true
		])
		
        logInfo("Updating application [$application] process [$process] version [${ucdApplicationProcess.getVersion()}].")
		
		// If an required role ID was provided then use it. Otherwise get the role information to get the ID.
		String requiredRoleId = requiredRole
		if (requiredRole && !UcdObject.isUUID(requiredRole)) {
			UcdRole ucdRole = actionsRunner.runAction([
				action: UcdGetRole.getSimpleName(),
				actionInfo: false,
				role: requiredRole,
				failIfNotFound: true
			])
			requiredRoleId = ucdRole.getId()
		}

		// Create a request map providing any newly specified values, otherwise using the existing values.
		Map requestMap = [
			applicationId : ucdApplicationProcess.getApplication().getId(),
			existingId : ucdApplicationProcess.getId(),
			name : ucdApplicationProcess.getName(),
			applicationProcessVersion : ucdApplicationProcess.getVersion(),
			description : description ?: ucdApplicationProcess.getDescription() ?: "",
			inventoryManagementType : inventoryManagementType ?: ucdApplicationProcess.getInventoryManagementType(),
			offlineAgentHandling : offlineAgentHandling ?: ucdApplicationProcess.getOfflineAgentHandling(),
			requiredRoleId : ((requiredRole != null) ? requiredRoleId : ucdApplicationProcess.getRequiredRoleId())
		]
		
		JsonBuilder jsonBuilder = new JsonBuilder(requestMap)
			
		WebTarget target = ucdSession.getUcdWebTarget().path("/rest/deploy/applicationProcess")
		logDebug("target=$target")

		Response response = target.request(MediaType.APPLICATION_JSON).put(Entity.json(jsonBuilder.toString()))
		if (response.getStatus() == 200) {
			logInfo("Application [$application] process [$process] basic information updated.")
		} else {
            throw new UcdInvalidValueException(response)
		}
	}
}
