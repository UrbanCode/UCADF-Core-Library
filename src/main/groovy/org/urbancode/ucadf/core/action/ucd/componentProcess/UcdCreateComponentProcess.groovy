/**
 * This action creates an component process.
 */
package org.urbancode.ucadf.core.action.ucd.componentProcess

import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.action.ucd.component.UcdGetComponent
import org.urbancode.ucadf.core.action.ucd.role.UcdGetRole
import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucd.component.UcdComponent
import org.urbancode.ucadf.core.model.ucd.general.UcdObject
import org.urbancode.ucadf.core.model.ucd.role.UcdRole

import groovy.json.JsonBuilder

class UcdCreateComponentProcess extends UcAdfAction {
	// Action properties.
	/** The component name or ID. */
	String component
	
	/** The component process name. */
	String name
	
	/** (Optional) The component process description. */
	String description = ""
	
	/** The process type. */
	UcdCreateComponentProcessTypeEnum type
	
	/** The inventory status. */
	String inventoryStatus = "Active"
	
	/** The default working directory. */
	String defaultWorkingDirectory = '''${p:resource/work.dir}/${p:component.name}'''
	
	/** (Optional) The required role. */
	String requiredRole = ""
	
	/** The flag that indicates fail if the component process already exists. Default is true. */
	Boolean failIfExists = true

	/**
	 * Runs the action.	
	 * @return True if the component process was created.
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExist()

		Boolean created = false

		// Get the component ID.				
		String componentId = component
		if (!UcdObject.isUUID(component)) {
			UcdComponent ucdComponent = actionsRunner.runAction([
				action: UcdGetComponent.getSimpleName(),
				actionInfo: false,
				component: component,
				failIfNotFound: true
			])
			componentId = ucdComponent.getId()
		}
		
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

		logVerbose("Creating component [$component] process [$name].")

		String inventoryActionType
		String configActionType
		Boolean takesVersion
		
		Map requestMap = [
			name: name,
			description: description,
			defaultWorkingDirectory: defaultWorkingDirectory,
			requiredRoleId: requiredRoleId,
			componentId: componentId
		]

		switch (type) {
			case UcdCreateComponentProcessTypeEnum.DEPLOYMENT:
				requestMap.put('status', inventoryStatus)
				requestMap.put('inventoryActionType', "ADD")
				requestMap.put('configActionType', "ADD")
				requestMap.put('takesVersion', "true")
				break
				
			case UcdCreateComponentProcessTypeEnum.CONFIGURATIONDEPLOYMENT:
				requestMap.put('configActionType', "ADD")
				requestMap.put('takesVersion', "false")
				break
				
			case UcdCreateComponentProcessTypeEnum.UNINSTALL:
				requestMap.put('status', inventoryStatus)
				requestMap.put('inventoryActionType', "REMOVE")
				requestMap.put('takesVersion', "true")
				break

			case UcdCreateComponentProcessTypeEnum.OPERATIONALWITHVERSION:				
				requestMap.put('takesVersion', "true")
				break

			case UcdCreateComponentProcessTypeEnum.OPERATIONALNOVERSION:	
				requestMap.put('takesVersion', "false")
		}
		
		JsonBuilder jsonBuilder = new JsonBuilder(requestMap)
		logVerbose(jsonBuilder.toString())
		
		WebTarget target = ucdSession.getUcdWebTarget().path("/rest/deploy/componentProcess")
		logDebug("target=$target")
		
		Response response = target.request(MediaType.APPLICATION_JSON).put(Entity.json(jsonBuilder.toString()))
		if (response.getStatus() == 200) {
			logVerbose("Component [$component] process [$name] created.")
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
	
	/**
	 * Component process type enumeration.
	 */
	enum UcdCreateComponentProcessTypeEnum {
		DEPLOYMENT,
		CONFIGURATIONDEPLOYMENT,
		UNINSTALL,
		OPERATIONALWITHVERSION,
		OPERATIONALNOVERSION
	}
}
