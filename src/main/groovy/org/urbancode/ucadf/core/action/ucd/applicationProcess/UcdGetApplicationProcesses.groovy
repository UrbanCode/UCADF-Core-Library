/**
 * This action gets a list of application processes.
 */
package org.urbancode.ucadf.core.action.ucd.applicationProcess

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.GenericType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.action.ucd.application.UcdGetApplication
import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.application.UcdApplication
import org.urbancode.ucadf.core.model.ucd.applicationProcess.UcdApplicationProcess
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucd.general.UcdObject

class UcdGetApplicationProcesses extends UcAdfAction {
	// Action properties.
	/** The application name or ID. */
	String application

	/** If true then gets the full information for the application process. */	
	Boolean full = false
	
	/**
	 * Runs the action.	
	 * @return The list of application process objects.
	 */
	@Override
	public List<UcdApplicationProcess> run() {
		// Validate the action properties.
		validatePropsExist()

		logVerbose("Getting application [$application] processes full [$full].")
		
		// If an application ID was provided then use it. Otherwise get the application information to get the ID.
		String applicationId = application
		if (!UcdObject.isUUID(application)) {
			UcdApplication ucdApplication = actionsRunner.runAction([
				action: UcdGetApplication.getSimpleName(),
				actionInfo: false,
				actionVerbose: false,
				application: application,
				failIfNotFound: true
			])
			applicationId = ucdApplication.getId()
		}
		
		List<UcdApplicationProcess> ucdApplicationProcesses = []
		
		WebTarget target 
		if (full) {
			target = ucdSession.getUcdWebTarget().path("/rest/deploy/application/{appId}/fullProcesses")
				.resolveTemplate("appId", applicationId)
			logDebug("target=$target")
		} else {
			target = ucdSession.getUcdWebTarget().path("/rest/deploy/application/{appId}/processes/active")
				.resolveTemplate("appId", applicationId)
			logDebug("target=$target")
		}

		Response response = target.request().get()
		if (response.getStatus() == 200) {
			ucdApplicationProcesses = response.readEntity(new GenericType<List<UcdApplicationProcess>>(){})
		} else {
			throw new UcAdfInvalidValueException(response)
		}

		return ucdApplicationProcesses
	}	
}
