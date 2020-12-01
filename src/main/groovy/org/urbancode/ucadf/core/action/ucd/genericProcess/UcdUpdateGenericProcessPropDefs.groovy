/**
 * This action updates a generic process property definitions.
 */
package org.urbancode.ucadf.core.action.ucd.genericProcess

import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucd.genericProcess.UcdGenericProcess
import org.urbancode.ucadf.core.model.ucd.property.UcdPropDef

import groovy.json.JsonBuilder

class UcdUpdateGenericProcessPropDefs extends UcAdfAction {
	// Action properties.
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
			UcdGenericProcess ucdGenericProcess = actionsRunner.runAction([
				action: UcdGetGenericProcess.getSimpleName(),
				actionInfo: false,
				actionVerbose: false,
				process: process,
				failIfNotFound: true
			])
	
			// Find the existing property definition.
			UcdPropDef existingUcdPropDef = ucdGenericProcess.getPropDefByName(propDef.getName())
			if (!existingUcdPropDef) {
				throw new UcAdfInvalidValueException("Process [$process] property name [${propDef.getName()}] not found.")
			}
	
			logVerbose("Updating process [$process] property definition [${propDef.getName()}].")
	
			WebTarget target = ucdSession.getUcdWebTarget().path("/rest/process/{processId}/propDefs")
				.resolveTemplate("processId", ucdGenericProcess.getId())
			logDebug("target=$target")
	
			// Merge the request update properties into the existing property definition.
			Map requestMap = existingUcdPropDef.deriveRequestMap(
				ucdGenericProcess,
				propDef
			)
			
			JsonBuilder jsonBuilder = new JsonBuilder(requestMap)
			logVerbose(jsonBuilder.toString())
			
			Response response = target.request(MediaType.APPLICATION_JSON).put(Entity.json(jsonBuilder.toString()))
			if (response.getStatus() == 200) {
				logVerbose("Process [$process] property definition [${propDef.getName()}] updated.")
			} else {
	            throw new UcAdfInvalidValueException(response)
			}
		}
	}
}
