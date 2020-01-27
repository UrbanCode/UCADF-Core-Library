/**
 * This action gets a generic process.
 */
package org.urbancode.ucadf.core.action.ucd.genericProcess

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.general.UcdObject
import org.urbancode.ucadf.core.model.ucd.genericProcess.UcdGenericProcess

class UcdGetGenericProcess extends UcAdfAction {
	/** The generic process name or ID. */
	String process
	
	/** The version. Default is -1 (latest). */
	Long version = -1
	
	/** The flag that indicates fail if the generic process is not found. Default is true. */
	Boolean failIfNotFound = true
	
	/**
	 * Runs the action.	
	 * @return The generic process object.
	 */
	@Override
	public UcdGenericProcess run() {
		// Validate the action properties.
		validatePropsExist()

		UcdGenericProcess ucdGenericProcess

		logDebug("Getting generic process [$process].")

		// If an generic process ID was provided then use it. Otherwise get the generic process information to get the ID.
		String processId
		if (UcdObject.isUUID(process)) {
			processId = process
		} else {
			// No API found to get a single template by name so have to get the map of all templates then select the one from it
			List<UcdGenericProcess> ucdGenericProcesss = actionsRunner.runAction([
				action: UcdGetGenericProcesses.getSimpleName()
			])
	
			UcdGenericProcess ucdFindGenericProcess = ucdGenericProcesss.find {
				(it.getName() == process)
			}

			if (ucdFindGenericProcess) {
				processId = ucdFindGenericProcess.getId()
			} else {
				if (failIfNotFound) {
					throw new UcdInvalidValueException("Generic process [$process] not found.")
				}
			}
		}
		
		// Get the generic process details.
		if (processId) {
			WebTarget target = ucdSession.getUcdWebTarget().path("/rest/process/{processId}/{version}")
				.resolveTemplate("processId", processId)
				.resolveTemplate("version", version)
			logDebug("target=$target")
			
			Response response = target.request().get()
			if (response.getStatus() == 200) {
				ucdGenericProcess = response.readEntity(UcdGenericProcess.class)
			} else {
				String errMsg = UcdInvalidValueException.getResponseErrorMessage(response)
				logVerbose(errMsg)
				if (response.getStatus() == 404 || response.getStatus() == 403) {
					if (failIfNotFound) {
						throw new UcdInvalidValueException(errMsg)
					}
				} else {
					throw new UcdInvalidValueException(errMsg)
				}
			}
		}

		return ucdGenericProcess
	}
}
