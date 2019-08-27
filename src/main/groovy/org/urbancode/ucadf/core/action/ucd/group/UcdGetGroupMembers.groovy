/**
 * This action gets list of group members.
 */
package org.urbancode.ucadf.core.action.ucd.group

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.GenericType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.user.UcdUser

class UcdGetGroupMembers extends UcAdfAction {
	// Action properties.
	/** The group name or ID. */
	String group
	
	/**
	 * Runs the action.	
	 * @return The list of user objects.
	 */
	@Override
	public List<UcdUser> run() {
		// Validate the action properties.
		validatePropsExist()

		List<UcdUser> ucdUsers = []
		
        WebTarget target = ucdSession.getUcdWebTarget().path("/security/group/{group}/members")
			.resolveTemplate("group", group)
		logDebug("target=$target")
		
		Response response = target.request().get()
		if (response.getStatus() == 200) {
			ucdUsers = response.readEntity(new GenericType<List<UcdUser>>(){})
		} else {
            throw new UcdInvalidValueException(response)
		}
				
		return ucdUsers
	}
}
