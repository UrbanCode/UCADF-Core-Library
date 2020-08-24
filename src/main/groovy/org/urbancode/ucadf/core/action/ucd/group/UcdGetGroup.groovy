/**
 * This action gets a group.
 */
package org.urbancode.ucadf.core.action.ucd.group

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucd.group.UcdGroup

class UcdGetGroup extends UcAdfAction {
	// Action properties.
	/** The group name or ID. */
	String group
	
	/** The authorization realm name or ID. (Optional. If not provided then unpredictable if more than one group with the same name. */
	String authorizationRealm = ""
	
	/** The flag that indicates fail if the group is not found. Default is true. */
	Boolean failIfNotFound = true
	
	/**
	 * Runs the action.	
	 * @return The group object.
	 */
	@Override
	public UcdGroup run() {
		// Validate the action properties.
		validatePropsExist()

		UcdGroup ucdGroup

		// If an authorization realm ID was provided then use it. Otherwise get the authorization realm information to get the ID.
		if (authorizationRealm) {
			// Get the list of groups.		
			List<UcdGroup> ucdGroups = actionsRunner.runAction([
				action: UcdGetGroups.getSimpleName(),
				actionInfo: false,
				actionVerbose: false
			])
	
			// Find the matching authorization realm and group name.
			ucdGroup = ucdGroups.find {
				(authorizationRealm.equals(it.getAuthorizationRealm().getId()) || authorizationRealm.equals(it.getAuthorizationRealm().getName())) && (group.equals(it.getName()) || group .equals(it.getId()))
			}
			
			if (!ucdGroup && failIfNotFound) {
				throw new UcAdfInvalidValueException("Group [$group] not found in authorization realm [$authorizationRealm].")
			}
		} else {
	        WebTarget target = ucdSession.getUcdWebTarget().path("/security/group/{group}")
				.resolveTemplate("group", group)
			logDebug("target=$target")
			
			Response response = target.request().get()
			if (response.getStatus() == 200) {
				ucdGroup = response.readEntity(UcdGroup.class)
			} else {
				String errMsg = UcAdfInvalidValueException.getResponseErrorMessage(response)
				logVerbose(errMsg)
				if ((response.getStatus() != 400 && response.getStatus() != 404 && response.getStatus() != 500) || failIfNotFound) {
					throw new UcAdfInvalidValueException(errMsg)
				}
			}
		}
		
		return ucdGroup
	}
}
