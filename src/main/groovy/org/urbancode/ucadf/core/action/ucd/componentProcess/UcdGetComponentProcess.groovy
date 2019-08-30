/**
 * This action gets a component process.
 */
package org.urbancode.ucadf.core.action.ucd.componentProcess

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.componentProcess.UcdComponentProcess
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.general.UcdObject

class UcdGetComponentProcess extends UcAdfAction {
	// Action properties.
	/** The component name or ID. */
	String component

	/** The process name or ID. */	
	String process
	
	/** If true then get the full information for the component process. Default is true. */
	Boolean full = true
	
	/** The flag that indicates fail if the component process is not found. Default is true. */
	Boolean failIfNotFound = true
	
	/**
	 * Runs the action.	
	 * @return The component process object.
	 */
	@Override
	public UcdComponentProcess run() {
		// Validate the action properties.
		validatePropsExist()

		logInfo("Getting component [$component] process [$process].")
	
		UcdComponentProcess ucdComponentProcess 

		if (full) {		
			// Get full component process information.
			if (UcdObject.isUUID(process)) {
				ucdComponentProcess = getComponentProcessFull(
					process,
					failIfNotFound
				)
			} else {
				// Must have a ID to get the full information.
				ucdComponentProcess = getComponentProcessInfo(
					process,
					failIfNotFound
				)
				
				if (ucdComponentProcess) {			
					ucdComponentProcess = getComponentProcessFull(
						ucdComponentProcess.getId(),
						failIfNotFound
					)
				}
			}
		} else {
			// Get basic component process information.
			ucdComponentProcess = getComponentProcessInfo(
				process,
				failIfNotFound
			)
		}
		
		return ucdComponentProcess
	}

	// Get the basic component process information.
	public UcdComponentProcess getComponentProcessInfo(
		final String process,
		final Boolean failIfNotFound) {
		
		UcdComponentProcess ucdComponentProcess
		
		// Get the basic component process information.
		WebTarget target = ucdSession.getUcdWebTarget().path("/cli/componentProcess/info")
			.queryParam("component", component)
			.queryParam("componentProcess", process)
		logDebug("target=$target")

		Response response = target.request().get()
		if (response.getStatus() == 200) {
			ucdComponentProcess = response.readEntity(UcdComponentProcess.class)
		} else {
			String errMsg = "${response.getStatus()} Component [$component] process [$process] not found."
			logInfo(errMsg)
			if (response.getStatus() == 204 || response.getStatus() == 404 || response.getStatus() == 403) {
				// 403 added for UCD 16.2. 204 added for UCD 17.1.
				if (failIfNotFound) {
					throw new UcdInvalidValueException(errMsg)
				}
			} else {
				throw new UcdInvalidValueException(errMsg)
			}
		}
		
		return ucdComponentProcess
	}

	// Get the full component process information.	
	public UcdComponentProcess getComponentProcessFull(
		final String processId,
		final Boolean failIfNotFound) {
		
		UcdComponentProcess ucdComponentProcess
		
		// Get the full information for the component process.
		WebTarget target = ucdSession.getUcdWebTarget().path("/rest/deploy/componentProcess/{processId}/-1")
			.resolveTemplate("processId", processId)
		logDebug("target=$target")
		
		Response response = target.request().get()
		if (response.getStatus() == 200) {
			ucdComponentProcess = response.readEntity(UcdComponentProcess.class)
		} else {
			String errMsg = UcdInvalidValueException.getResponseErrorMessage(response)
			logInfo(errMsg)
			if (response.getStatus() == 404 || response.getStatus() == 403) {
				if (failIfNotFound) {
					throw new UcdInvalidValueException(errMsg)
				}
			} else {
				throw new UcdInvalidValueException(errMsg)
			}
		}
		
		return ucdComponentProcess
	}
}
