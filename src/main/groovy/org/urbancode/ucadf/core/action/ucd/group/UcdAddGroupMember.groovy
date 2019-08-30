/**
 * This action adds a group member.
 */
package org.urbancode.ucadf.core.action.ucd.group

import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.action.ucd.user.UcdGetUser
import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.general.UcdObject
import org.urbancode.ucadf.core.model.ucd.group.UcdGroup
import org.urbancode.ucadf.core.model.ucd.user.UcdUser

class UcdAddGroupMember extends UcAdfAction {
	/** The group name or ID. */
	String group
	
	/** The user name or ID. */
	String user
	
	/**
	 * Runs the action.	
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExist()

		logInfo("Add user [$user] to group [$group].")
		
		// If an group ID was provided then use it. Otherwise get the group information to get the ID.
		String groupId = group
		if (!UcdObject.isUUID(group)) {
			UcdGroup ucdGroup = actionsRunner.runAction([
				action: UcdGetGroup.getSimpleName(),
				group: group,
				failIfNotFound: true
			])
			
			groupId = ucdGroup.getId()
		}
		
		// If an user ID was provided then use it. Otherwise get the user information to get the ID.
		String userId = user
		if (!UcdObject.isUUID(user)) {
			UcdUser ucdUser = actionsRunner.runAction([
				action: UcdGetUser.getSimpleName(),
				user: user,
				failIfNotFound: true
			])
			userId = ucdUser.getId()
		}
		
		WebTarget target = ucdSession.getUcdWebTarget().path("/security/group/{groupId}/members/{userId}")
			.resolveTemplate("groupId", groupId)
			.resolveTemplate("userId", userId)
		logDebug("target=$target")
		
		Response response = target.request(MediaType.APPLICATION_JSON).put(Entity.text(""))
		if (response.getStatus() == 200) {
			logInfo("User [$user] added to group [$group].")
		} else {
			throw new UcdInvalidValueException(response)
		}
	}
}
