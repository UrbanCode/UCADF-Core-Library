/**
 * This action deletes an application process property definitions.
 */
package org.urbancode.ucadf.core.action.ucd.applicationProcess

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucd.applicationProcess.UcdApplicationProcess
import org.urbancode.ucadf.core.model.ucd.property.UcdPropDef

class UcdDeleteApplicationProcessPropDefs extends UcAdfAction {
	// Action properties.
	/** The application name or ID. If not specified then process must be an ID. */
	String application = ""
	
	/** The process name or ID. */
	String process

	/** The property names. */
	List<String> names

	/** The flag that indicates fail if the property definition is not found. Default is false. */
	Boolean failIfNotFound = false
	
	/**
	 * Runs the action.	
	 * @return True if the application process property definition was deleted.
	 */
	@Override
	public Boolean run() {
		// Validate the action properties.
		validatePropsExist()
		
		Boolean deleted = false
			
		for (name in names) {
			UcdApplicationProcess ucdApplicationProcess = actionsRunner.runAction([
				action: UcdGetApplicationProcess.getSimpleName(),
				actionInfo: false,
				actionVerbose: false,
				application: application,
				process: process,
				failIfNotFound: true
			])
	
			// Find the existing property definition.
			UcdPropDef existingUcdPropDef = ucdApplicationProcess.getPropDefByName(name)
			if (!existingUcdPropDef && failIfNotFound) {
				throw new UcAdfInvalidValueException("Application [$application] process [$process] property definition [$name] not found.")
			}
	
			if (existingUcdPropDef) {
				logVerbose("Deleting application [$application] process [$process] property definition [$name].")
	
				WebTarget target = ucdSession.getUcdWebTarget().path("/rest/deploy/applicationProcess/{processId}/deletePropDef")
					.resolveTemplate("processId", ucdApplicationProcess.getId())
					.queryParam("propName", name)
				logDebug("target=$target")
		
				Response response = target.request(MediaType.APPLICATION_JSON)
					.header("applicationProcessVersion", ucdApplicationProcess.getVersionCount())
					.delete()
					
				if (response.getStatus() == 204) {
					logVerbose("Application [$application] process [$process] property [$name] deleted.")
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
