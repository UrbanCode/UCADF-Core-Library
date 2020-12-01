/**
 * This action adds an application process property definitions.
 */
package org.urbancode.ucadf.core.action.ucd.applicationProcess

import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucd.applicationProcess.UcdApplicationProcess
import org.urbancode.ucadf.core.model.ucd.property.UcdPropDef

import groovy.json.JsonBuilder

class UcdAddApplicationProcessPropDefs extends UcAdfAction {
	// Action properties.
	/** The application name or ID. If not specified then process must be an ID. */
	String application = ""
	
	/** The process name or ID. */
	String process

	/** The property definitions. */
	List<UcdPropDef> propDefs

	/** The flag that indicates fail if the property definition already exists. Default is true. */
	Boolean failIfExists = true
	
	/**
	 * Runs the action.	
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExist()
		
		for (propDef in propDefs) {
			UcdApplicationProcess ucdApplicationProcess = actionsRunner.runAction([
				action: UcdGetApplicationProcess.getSimpleName(),
				actionInfo: false,
				actionVerbose: false,
				application: application,
				process: process,
				failIfNotFound: true
			])
	
			// Find the existing property definition.
			UcdPropDef existingUcdPropDef = ucdApplicationProcess.getPropDefByName(propDef.getName())
			if (existingUcdPropDef && failIfExists) {
				throw new UcAdfInvalidValueException("Application [$application] process [$process] property name [${propDef.getName()}] already exists.")
			}
	
			if (existingUcdPropDef) {
				logVerbose("Application [$application] process [$process] property definition [${propDef.getName()}] already exists.")
			} else {
				logVerbose("Adding process [$process] property definition [${propDef.getName()}].")
		
				WebTarget target = ucdSession.getUcdWebTarget().path("/rest/deploy/applicationProcess/{processId}/savePropDef")
					.resolveTemplate("processId", ucdApplicationProcess.getId())
				logDebug("target=$target")
		
				// Merge the request update properties into the existing property definition.
				Map requestMap = propDef.deriveRequestMap(ucdApplicationProcess)
				
				JsonBuilder jsonBuilder = new JsonBuilder(requestMap)
				logVerbose(jsonBuilder.toString())
				
				Response response = target.request(MediaType.APPLICATION_JSON).put(Entity.json(jsonBuilder.toString()))
				if (response.getStatus() == 200) {
					logVerbose("Application [$application] process [$process] property definition [${propDef.getName()}] added.")
				} else {
		            throw new UcAdfInvalidValueException(response)
				}
			}
		}
	}
}
