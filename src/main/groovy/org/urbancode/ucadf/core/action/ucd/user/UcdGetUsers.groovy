/**
 * This action gets users.
 */
package org.urbancode.ucadf.core.action.ucd.user

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.GenericType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucd.user.UcdUser

class UcdGetUsers extends UcAdfAction {
	// Action properties.
	/** (Optional) If specified then get users with names that match this regular expression. */
	String match = ""
	
	/**
	 * Runs the action.	
	 * @return The list of user objects.
	 */
	@Override
	public List<UcdUser> run() {
		// Validate the action properties.
		validatePropsExist()

		List<UcdUser> ucdUsers = []
		
		WebTarget target = ucdSession.getUcdWebTarget().path("/security/user")
		logDebug("target=$target")
		
		Response response = target.request().get()
		if (response.getStatus() == 200) {
			ucdUsers = response.readEntity(new GenericType<List<UcdUser>>(){})
		} else {
            throw new UcAdfInvalidValueException(response)
		}

		List<UcdUser> ucdReturnUsers = []
		
		if (match) {
			for (ucdUser in ucdUsers) {
				if (ucdUser.getName() ==~ match) {
					ucdReturnUsers.add(ucdUser)
				}
			}
		} else {
			ucdReturnUsers = ucdUsers
		}
		
		return ucdReturnUsers
	}
}
