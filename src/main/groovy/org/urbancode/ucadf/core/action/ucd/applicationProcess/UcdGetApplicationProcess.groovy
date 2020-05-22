/**
 * This action gets an application process.
 */
package org.urbancode.ucadf.core.action.ucd.applicationProcess

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.applicationProcess.UcdApplicationProcess
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucd.general.UcdObject

class UcdGetApplicationProcess extends UcAdfAction {
	// Action properties.
	/** The application name or ID. If not specified then process must be an ID. */
	String application = ""
	
	/** The process name or ID. */
	String process

	/** The version of the process. Default is -1. */	
	Long version = -1
	
	/** The flag that indicates to return the full information about the process. */
	Boolean full = true
	
	/** The flag that indicates fail if the application process is not found. Default is true. */
	Boolean failIfNotFound = true
	
	/**
	 * Runs the action.	
	 * @return The application process object.
	 */
	@Override
	public UcdApplicationProcess run() {
		// Validate the action properties.
		validatePropsExistExclude([ 'application' ])

		logVerbose("Getting application [$application] process [$process].")
	
		UcdApplicationProcess ucdApplicationProcess 

		if (full) {		
			// Get full application process information.
			if (UcdObject.isUUID(process)) {
				ucdApplicationProcess = getApplicationProcessFull(
					process,
					failIfNotFound
				)
			} else {
				// Must have a ID to get the full information.
				ucdApplicationProcess = getApplicationProcessInfo(
					process,
					failIfNotFound
				)
				
				if (ucdApplicationProcess) {			
					ucdApplicationProcess = getApplicationProcessFull(
						ucdApplicationProcess.getId(),
						failIfNotFound
					)
				}
			}
		} else {
			// Get basic application process information.
			ucdApplicationProcess = getApplicationProcessInfo(
				process,
				failIfNotFound
			)
		}
		
		return ucdApplicationProcess
	}

	// Get the basic application process information.
	public UcdApplicationProcess getApplicationProcessInfo(
		final String process,
		final Boolean failIfNotFound) {
		
		UcdApplicationProcess ucdApplicationProcess
		
		// Get the basic application process information.
		WebTarget target = ucdSession.getUcdWebTarget().path("/cli/applicationProcess/info")
			.queryParam("application", application)
			.queryParam("applicationProcess", process)
		logDebug("target=$target")

		Response response = target.request().get()
		if (response.getStatus() == 200) {
			ucdApplicationProcess = response.readEntity(UcdApplicationProcess.class)
		} else {
			String errMsg = "${response.getStatus()} Application [$application] process [$process] not found."
			logVerbose(errMsg)
			if (response.getStatus() == 204 || response.getStatus() == 404 || response.getStatus() == 403) {
				// 403 added for UCD 16.2. 204 added for UCD 17.1.
				if (failIfNotFound) {
					throw new UcAdfInvalidValueException(errMsg)
				}
			} else {
				throw new UcAdfInvalidValueException(errMsg)
			}
		}
		
		return ucdApplicationProcess
	}

	// Get the full application process information.	
	public UcdApplicationProcess getApplicationProcessFull(
		final String processId,
		final Boolean failIfNotFound) {
		
		UcdApplicationProcess ucdApplicationProcess
		
		// Get the full information for the application process.
		WebTarget target = ucdSession.getUcdWebTarget().path("/rest/deploy/applicationProcess/{processId}/{version}")
			.resolveTemplate("processId", processId)
			.resolveTemplate("version", version)
		logDebug("target=$target")
		
		Response response = target.request().get()
		if (response.getStatus() == 200) {
			ucdApplicationProcess = response.readEntity(UcdApplicationProcess.class)
		} else {
			String errMsg = UcAdfInvalidValueException.getResponseErrorMessage(response)
			logVerbose(errMsg)
			if (response.getStatus() == 404 || response.getStatus() == 403) {
				// 403 added for UCD 16.2
				if (failIfNotFound) {
					throw new UcAdfInvalidValueException(errMsg)
				}
			} else {
				throw new UcAdfInvalidValueException(errMsg)
			}
		}
		
		return ucdApplicationProcess
	}
}
