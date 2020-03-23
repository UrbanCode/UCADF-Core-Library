/**
 * This action gets a list of applications.
 */
package org.urbancode.ucadf.core.action.ucd.application

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.GenericType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.application.UcdApplication
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException

class UcdGetApplications extends UcAdfAction {
	// Action properties.
	/** (Optional) If specified then get applications with names that match this regular expression. */
	String match = ""
	
	/**
	 * Runs the action.
	 * @return The list of application objects.
	 */
	public List<UcdApplication> run() {
		// Validate the action properties.
		validatePropsExist()

		List<UcdApplication> ucdApplications
		
		WebTarget target = ucdSession.getUcdWebTarget().path("/cli/application")
		logDebug("target=$target")
		
		Response response = target.request().get()
		if (response.getStatus() == 200) {
			ucdApplications = response.readEntity(new GenericType<List<UcdApplication>>(){})
		} else {
            throw new UcAdfInvalidValueException(response)
		}
		
		List<UcdApplication> ucdReturnApplications = []
		
		if (match) {
			for (ucdApplication in ucdApplications) {
				if (ucdApplication.getName() ==~ match) {
					ucdReturnApplications.add(ucdApplication)
				}
			}
		} else {
			ucdReturnApplications = ucdApplications
		}
		
		return ucdReturnApplications
	}
}
