/**
 * This action removes a group member.
 */
package org.urbancode.ucadf.core.action.ucd.group

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.action.ucd.user.UcdGetUser
import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucd.general.UcdObject
import org.urbancode.ucadf.core.model.ucd.group.UcdGroup
import org.urbancode.ucadf.core.model.ucd.user.UcdUser

class UcdRemoveGroupMember extends UcAdfAction {
	/** The group name or ID. */
	String group
	
	/** The authorization realm name or ID. (Optional. If not provided then unpredictable if more than one group with the same name. */
	String authorizationRealm = ""
	
	/** The user name or ID. */
	String user
	
	/**
	 * Runs the action.	
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExist()

		logVerbose("Remove user [$user] from group [$group].")
		
		// If an group ID was provided then use it. Otherwise get the group information to get the ID.
		UcdGroup ucdGroup = actionsRunner.runAction([
			action: UcdGetGroup.getSimpleName(),
			actionInfo: false,
			actionVerbose: false,
			group: group,
			authorizationRealm: authorizationRealm,
			failIfNotFound: true
		])
		
		String groupId = ucdGroup.getId()
		
		// If an user ID was provided then use it. Otherwise get the user information to get the ID.
		String userId = user
		if (!UcdObject.isUUID(user)) {
			UcdUser ucdUser = actionsRunner.runAction([
				action: UcdGetUser.getSimpleName(),
				actionInfo: false,
				actionVerbose: false,
				user: user,
				failIfNotFound: true
			])
			userId = ucdUser.getId()
		}
		
		WebTarget target = ucdSession.getUcdWebTarget().path("/security/group/{groupId}/members/{userId}")
			.resolveTemplate("groupId", groupId)
			.resolveTemplate("userId", userId)
		logDebug("target=$target")
		
		Response response = target.request(MediaType.APPLICATION_JSON).delete()
		if (response.getStatus() == 200) {
			logVerbose("User [$user] removed group [$group].")
		} else {
			throw new UcAdfInvalidValueException(response)
		}
	}
}
