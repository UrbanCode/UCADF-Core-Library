/**
 * This action gets a list of notification schemes.
 */
package org.urbancode.ucadf.core.action.ucd.notificationScheme

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.GenericType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucd.notificationScheme.UcdNotificationScheme

class UcdGetNotificationSchemes extends UcAdfAction {
	/** (Optional) If specified then gets notification schemes with names that match this regular expression. */
	String match = ""

	/**
	 * Runs the action.	
	 * @return The list of notification scheme objects.
	 */
	@Override
	public List<UcdNotificationScheme> run() {
		// Validate the action properties.
		validatePropsExist()

		List<UcdNotificationScheme> ucdNotificationSchemes
		List<UcdNotificationScheme> ucdReturnNotificationSchemes = []
		
		WebTarget target = ucdSession.getUcdWebTarget().path("/rest/notification/notificationScheme")
		logDebug("target=$target")
	
		Response response = target.request().get()
		if (response.getStatus() == 200) {
			ucdNotificationSchemes = response.readEntity(new GenericType<List<UcdNotificationScheme>>(){})
		} else {
            throw new UcAdfInvalidValueException(response)
		}
		
		if (match) {		
			for (ucdNotificationScheme in ucdNotificationSchemes) {
				if (match && !(ucdNotificationScheme.getName() ==~ match)) {
					continue
				}

				ucdReturnNotificationSchemes.add(ucdNotificationScheme)
			}
		} else {
			ucdReturnNotificationSchemes = ucdNotificationSchemes
		}
		
		return ucdReturnNotificationSchemes
	}
}
