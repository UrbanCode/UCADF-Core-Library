/**
 * This action deletes a component process property definitions.
 */
package org.urbancode.ucadf.core.action.ucd.componentProcess

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucd.componentProcess.UcdComponentProcess
import org.urbancode.ucadf.core.model.ucd.property.UcdPropDef

class UcdDeleteComponentProcessPropDefs extends UcAdfAction {
	// Action properties.
	/** The component name or ID. If not specified then process must be an ID. */
	String component = ""
	
	/** The process name or ID. */
	String process

	/** The property names. */
	List<String> names

	/** The flag that indicates fail if the property definition is not found. Default is false. */
	Boolean failIfNotFound = false
	
	/**
	 * Runs the action.	
	 * @return True if the component process property definition was deleted.
	 */
	@Override
	public Boolean run() {
		// Validate the action properties.
		validatePropsExist()
		
		Boolean deleted = false
			
		for (name in names) {
			UcdComponentProcess ucdComponentProcess = actionsRunner.runAction([
				action: UcdGetComponentProcess.getSimpleName(),
				actionInfo: false,
				actionVerbose: false,
				component: component,
				process: process,
				failIfNotFound: true
			])
	
			// Find the existing property definition.
			UcdPropDef existingUcdPropDef = ucdComponentProcess.getPropDefByName(name)
			if (!existingUcdPropDef && failIfNotFound) {
				throw new UcAdfInvalidValueException("Component [$component] process [$process] property definition [$name] not found.")
			}
	
			if (existingUcdPropDef) {
				logVerbose("Deleting component [$component] process [$process] property definition [$name].")
	
				WebTarget target = ucdSession.getUcdWebTarget().path("/rest/deploy/componentProcess/{processId}/deletePropDef")
					.resolveTemplate("processId", ucdComponentProcess.getId())
					.queryParam("propName", name)
				logDebug("target=$target")
		
				Response response = target.request(MediaType.APPLICATION_JSON)
					.header("componentProcessVersion", ucdComponentProcess.getVersionCount())
					.delete()
					
				if (response.getStatus() == 204) {
					logVerbose("Component [$component] process [$process] property [$name] deleted.")
					deleted = true
				} else {
					String errMsg = UcAdfInvalidValueException.getResponseErrorMessage(response)
					logVerbose(errMsg)
					if (response.getStatus() != 404 || failIfNotFound) {
						throw new UcAdfInvalidValueException(errMsg)
					}
				}
			}
		}
		
		return deleted
	}
}
