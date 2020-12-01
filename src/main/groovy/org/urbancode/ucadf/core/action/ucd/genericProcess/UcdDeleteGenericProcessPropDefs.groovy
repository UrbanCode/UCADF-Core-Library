/**
 * This action deletes a generic process property definitions.
 */
package org.urbancode.ucadf.core.action.ucd.genericProcess

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucd.genericProcess.UcdGenericProcess
import org.urbancode.ucadf.core.model.ucd.property.UcdPropDef

class UcdDeleteGenericProcessPropDefs extends UcAdfAction {
	// Action properties.
	/** The process name or ID. */
	String process

	/** The property names. */
	List<String> names

	/** The flag that indicates fail if the property definition is not found. Default is false. */
	Boolean failIfNotFound = false
	
	/**
	 * Runs the action.	
	 * @return True if the generic process property definition was deleted.
	 */
	@Override
	public Boolean run() {
		// Validate the action properties.
		validatePropsExist()
		
		Boolean deleted = false
			
		for (name in names) {
			UcdGenericProcess ucdGenericProcess = actionsRunner.runAction([
				action: UcdGetGenericProcess.getSimpleName(),
				actionInfo: false,
				actionVerbose: false,
				process: process,
				failIfNotFound: true
			])
	
			// Find the existing property definition.
			UcdPropDef existingUcdPropDef = ucdGenericProcess.getPropDefByName(name)
			if (!existingUcdPropDef && failIfNotFound) {
				throw new UcAdfInvalidValueException("Process [$process] property definition [$name] not found.")
			}
	
			if (existingUcdPropDef) {
				logVerbose("Deleting process [$process] property definition [$name].")
	
				WebTarget target = ucdSession.getUcdWebTarget().path("/rest/process/{processId}/propDefs")
					.resolveTemplate("processId", ucdGenericProcess.getId())
					.queryParam("propName", name)
				logDebug("target=$target")
		
				Response response = target.request(MediaType.APPLICATION_JSON)
					.header("processVersion", ucdGenericProcess.getVersionCount())
					.delete()
					
				if (response.getStatus() == 204) {
					logVerbose("Process [$process] property [$name] deleted.")
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
