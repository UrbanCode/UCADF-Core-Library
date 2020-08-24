/**
 * This action gets list of group members.
 */
package org.urbancode.ucadf.core.action.ucd.group

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.GenericType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucd.group.UcdGroup
import org.urbancode.ucadf.core.model.ucd.user.UcdUser

class UcdGetGroupMembers extends UcAdfAction {
	// Action properties.
	/** The group name or ID. */
	String group
	
	/** The authorization realm name or ID. (Optional. If not provided then unpredictable if more than one group with the same name. */
	String authorizationRealm = ""

	/**
	 * Runs the action.	
	 * @return The list of user objects.
	 */
	@Override
	public List<UcdUser> run() {
		// Validate the action properties.
		validatePropsExist()

		List<UcdUser> ucdUsers = []

		// Find the group by name and authorization realm.
		UcdGroup ucdGroup = actionsRunner.runAction([
			action: UcdGetGroup.getSimpleName(),
			actionInfo: false,
			actionVerbose: false,
			authorizationRealm: authorizationRealm,
			group: group,
			failIfNotFound: false
		])

		if (!ucdGroup) {
			throw new UcAdfInvalidValueException("Group [$group] authorization realm [$authorizationRealm] not found.")
		}
		
		String groupId = ucdGroup.getId()
		
        WebTarget target = ucdSession.getUcdWebTarget().path("/security/group/{group}/members")
			.resolveTemplate("group", groupId)
		logDebug("target=$target")
		
		Response response = target.request().get()
		if (response.getStatus() == 200) {
			ucdUsers = response.readEntity(new GenericType<List<UcdUser>>(){})
		} else {
            throw new UcAdfInvalidValueException(response)
		}
				
		return ucdUsers
	}
}
