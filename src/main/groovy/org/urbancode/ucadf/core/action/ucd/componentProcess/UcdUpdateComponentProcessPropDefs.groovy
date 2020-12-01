/**
 * This action updates a component process property definitions.
 */
package org.urbancode.ucadf.core.action.ucd.componentProcess

import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucd.componentProcess.UcdComponentProcess
import org.urbancode.ucadf.core.model.ucd.property.UcdPropDef

import groovy.json.JsonBuilder

class UcdUpdateComponentProcessPropDefs extends UcAdfAction {
	// Action properties.
	/** The component name or ID. If not specified then process must be an ID. */
	String component = ""
	
	/** The process name or ID. */
	String process

	/** The property definitions. */
	List<UcdPropDef> propDefs

	/**
	 * Runs the action.	
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExist()
		
		for (propDef in propDefs) {
			UcdComponentProcess ucdComponentProcess = actionsRunner.runAction([
				action: UcdGetComponentProcess.getSimpleName(),
				actionInfo: false,
				actionVerbose: false,
				component: component,
				process: process,
				failIfNotFound: true
			])
	
			// Find the existing property definition.
			UcdPropDef existingUcdPropDef = ucdComponentProcess.getPropDefByName(propDef.getName())
			if (!existingUcdPropDef) {
				throw new UcAdfInvalidValueException("Component [$component] process [$process] property name [${propDef.getName()}] not found.")
			}
	
			logVerbose("Updating component [$component] process [$process] property definition [${propDef.getName()}].")
	
			WebTarget target = ucdSession.getUcdWebTarget().path("/rest/deploy/componentProcess/{processId}/savePropDef")
				.resolveTemplate("processId", ucdComponentProcess.getId())
			logDebug("target=$target")
	
			// Merge the request update properties into the existing property definition.
			Map requestMap = existingUcdPropDef.deriveRequestMap(
				ucdComponentProcess,
				propDef
			)
			
			JsonBuilder jsonBuilder = new JsonBuilder(requestMap)
			logVerbose(jsonBuilder.toString())
			
			Response response = target.request(MediaType.APPLICATION_JSON).put(Entity.json(jsonBuilder.toString()))
			if (response.getStatus() == 200) {
				logVerbose("Component [$component] property definition [${propDef.getName()}] updated.")
			} else {
	            throw new UcAdfInvalidValueException(response)
			}
		}
	}
}
