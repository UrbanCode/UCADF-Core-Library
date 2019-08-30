/**
 * This action creates an application process.
 */
package org.urbancode.ucadf.core.action.ucd.applicationProcess

import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.action.ucd.role.UcdGetRole
import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.applicationProcess.UcdApplicationProcessInventoryManagementTypeEnum
import org.urbancode.ucadf.core.model.ucd.applicationProcess.UcdApplicationProcessOfflineAgentHandlingEnum
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.general.UcdObject
import org.urbancode.ucadf.core.model.ucd.property.UcdPropDef
import org.urbancode.ucadf.core.model.ucd.role.UcdRole

import groovy.json.JsonBuilder

class UcdCreateApplicationProcess extends UcAdfAction {
	// Action properties.
	/** The application name or ID. */
	String application
	
	/** The application process name. */
	String name
	
	/** (Optional) The application process description. */
	String description = ""
	
	/** The application process inventory management type. Default is Automatic. */
	UcdApplicationProcessInventoryManagementTypeEnum inventoryManagementType = UcdApplicationProcessInventoryManagementTypeEnum.AUTOMATIC
	
	/** The application process offling agent handling. Default is pre-execution check. */
	UcdApplicationProcessOfflineAgentHandlingEnum offlineAgentHandling = UcdApplicationProcessOfflineAgentHandlingEnum.PRE_EXECUTION_CHECK
	
	/** (Optional) The application process property definitions. */
	List<UcdPropDef> propDefs = []
	
	/** (Optional) The application process root activity. */
	Map rootActivity = [ type: "graph" ]
	
	/** (Optional) The required role. */
	String requiredRole = ""
	
	/** The flag that indicates fail if the application process already exists. Default is true. */
	Boolean failIfExists = true

	/**
	 * Runs the action.	
	 * @return True if the application process was created.
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExist()

		Boolean created = false
				
		logInfo("Creating application [$application] process [$name].")

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

		Map requestMap = [
			application: application,
			name: name,
			description: description,
			inventoryManagementType: inventoryManagementType,
			offlineAgentHandling: offlineAgentHandling,
			requiredRoleId: requiredRoleId,
			rootActivity: rootActivity,
			propDefs: propDefs
		]
		
		JsonBuilder jsonBuilder = new JsonBuilder(requestMap)
		

		WebTarget target = ucdSession.getUcdWebTarget().path("/cli/applicationProcess/create")
		logDebug("target=$target")
		
		Response response = target.request(MediaType.APPLICATION_JSON).put(Entity.json(jsonBuilder.toString()))
		if (response.getStatus() == 200) {
			logInfo("Application [$application] process [$name] created.")
			created = true
		} else {
			String errMsg = UcdInvalidValueException.getResponseErrorMessage(response)
			logInfo(errMsg)
			if (!(response.getStatus() == 400 && (errMsg ==~ /.*already exists.*/ && !failIfExists))) {
				throw new UcdInvalidValueException(errMsg)
			}
		}

		return created
	}
}
