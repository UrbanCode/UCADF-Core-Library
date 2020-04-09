/**
 * This action gets a list of application templates.
 */
package org.urbancode.ucadf.core.action.ucd.applicationTemplate

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.GenericType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucd.applicationTemplate.UcdApplicationTemplate

class UcdGetApplicationTemplates extends UcAdfAction {
	/** (Optional) If specified then gets application templates with names that match this regular expression. */
	String match = ""

	/**
	 * Runs the action.	
	 * @return The list of application template objects.
	 */
	@Override
	public List<UcdApplicationTemplate> run() {
		// Validate the action properties.
		validatePropsExist()

		List<UcdApplicationTemplate> ucdApplicationTemplates
		List<UcdApplicationTemplate> ucdReturnApplicationTemplates = []
		
		WebTarget target = ucdSession.getUcdWebTarget().path("/rest/deploy/applicationTemplate")
		logDebug("target=$target")
	
		Response response = target.request().get()
		if (response.getStatus() == 200) {
			ucdApplicationTemplates = response.readEntity(new GenericType<List<UcdApplicationTemplate>>(){})
		} else {
            throw new UcAdfInvalidValueException(response)
		}
		
		if (match) {		
			for (ucdApplicationTemplate in ucdApplicationTemplates) {
				if (match && !(ucdApplicationTemplate.getName() ==~ match)) {
					continue
				}

				ucdReturnApplicationTemplates.add(ucdApplicationTemplate)
			}
		} else {
			ucdReturnApplicationTemplates = ucdApplicationTemplates
		}
		
		return ucdReturnApplicationTemplates
	}
}
