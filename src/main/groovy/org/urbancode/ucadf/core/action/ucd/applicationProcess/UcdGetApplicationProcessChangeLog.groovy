/**
 * This action gets an application processes change log.
 */
package org.urbancode.ucadf.core.action.ucd.applicationProcess

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.GenericType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucd.applicationProcess.UcdApplicationProcess
import org.urbancode.ucadf.core.model.ucd.applicationProcess.UcdApplicationProcessCommit
import org.urbancode.ucadf.core.model.ucd.general.UcdObject

class UcdGetApplicationProcessChangeLog extends UcAdfAction {
	// Action properties.
	/** The application name or ID. If not specified then process must be an ID. */
	String application = ""
	
	/** The process name or ID. */
	String process

	/** The version of the process. Default is -1. */	
	Long version = -1
	
	/** The flag that indicates fail if the application process is not found. Default is true. */
	Boolean failIfNotFound = true
	
	/**
	 * Runs the action.	
	 * @return The list of application process log objects.
	 */
	@Override
	public List<UcdApplicationProcessCommit> run() {
		// Validate the action properties.
		validatePropsExistExclude([ 'application' ])

		logVerbose("Getting application [$application] process [$process] change log.")

		List<UcdApplicationProcessCommit> ucdApplicationProcessCommits
			
		String processId = process
		if (!UcdObject.isUUID(process)) {
			UcdApplicationProcess ucdApplication = actionsRunner.runAction([
				action: UcdGetApplicationProcess.getSimpleName(),
				actionInfo: false,
				application: application,
				process: process,
				failIfNotFound: failIfNotFound
			])
			processId = ucdApplication.getId()
		}
		
		// Get the basic application process information.
		WebTarget target = ucdSession.getUcdWebTarget().path("/rest/deploy/applicationProcess/{processId}/changelog")
			.resolveTemplate("processId", processId)
		logDebug("target=$target")

		Response response = target.request().get()
		if (response.getStatus() == 200) {
			ucdApplicationProcessCommits = response.readEntity(new GenericType<List<UcdApplicationProcessCommit>>(){})
		} else {
			throw new UcAdfInvalidValueException(response)
		}
		
		return ucdApplicationProcessCommits
	}
}
