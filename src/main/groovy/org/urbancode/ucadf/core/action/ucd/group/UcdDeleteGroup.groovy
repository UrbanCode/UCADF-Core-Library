/**
 * This action deletes a group from an authorization realm.
 */
package org.urbancode.ucadf.core.action.ucd.group

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucd.group.UcdGroup

class UcdDeleteGroup extends UcAdfAction {
	// Action properties.
	/** The group name. */
    String group
	
	/** The authorization realm name or ID. */
	String authorizationRealm
	
	/** The flag that indicates fail if not found. Default is false. */
	Boolean failIfNotFound = false

	/**
	 * Runs the action.	
	 * @return True if the group was deleted.
	 */
	@Override
	public Boolean run() {
		// Validate the action properties.
		validatePropsExist()

		Boolean deleted = false
		        
		// Find the group by name and authorization realm.
		UcdGroup ucdGroup = actionsRunner.runAction([
			action: UcdGetGroup.getSimpleName(),
			actionInfo: false,
			actionVerbose: false,
			authorizationRealm: authorizationRealm,
			group: group,
			failIfNotFound: failIfNotFound
		])

		if (ucdGroup) {
            // Delete the group
			WebTarget target = ucdSession.getUcdWebTarget().path("/security/group/{group}")
				.resolveTemplate("group", ucdGroup.getId())
			logDebug("target=$target")
			
			Response response = target.request(MediaType.APPLICATION_JSON).delete()
			if (response.getStatus() == 200 || response.getStatus() == 204) {
				logVerbose("Group [$group] deleted from authorization realm [$authorizationRealm].")
				deleted = true
			} else {
				throw new UcAdfInvalidValueException(response)
			}
		}
		
		return deleted
    }
}
