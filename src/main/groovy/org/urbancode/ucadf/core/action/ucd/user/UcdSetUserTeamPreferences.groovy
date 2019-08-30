/**
 * This action sets the user preferences default team mappings.
 */
package org.urbancode.ucadf.core.action.ucd.user

import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.action.ucd.team.UcdGetTeam
import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.team.UcdTeam
import org.urbancode.ucadf.core.model.ucd.user.UcdUserTeamMappingsEnum
import groovy.json.JsonBuilder
// Set the user team preferences.
// The session that is running this must have permissions to manage security in order to get the team ID.
class UcdSetUserTeamPreferences extends UcAdfAction {
	// Action properties.
	/** The team mapping type. */
	UcdUserTeamMappingsEnum defaultTeamMappingType
	
	/** (Optional) The team names. */
	List<String> teamNames = []
	
	/**
	 * Runs the action.	
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExist()

		// Validate a user ID and password were provided.
		if (!ucdUserId || !ucdUserPw || ucdAuthToken) {
			throw new UcdInvalidValueException("This action requires a value for ucdUserId and ucdUserPw, not ucdAuthToken.")
		}
		
		logInfo("Set user team preferences type [$defaultTeamMappingType] teamNames [$teamNames].")
		WebTarget target = ucdSession.getUcdWebTarget().path("/rest/security/userPreferences")
		logDebug("target=$target")
			
		// Build a custom body that includes only the required fields.
		Map requestMap = [
			defaultTeamMappingType: defaultTeamMappingType
		]
		JsonBuilder jsonBuilder = new JsonBuilder(requestMap)

		if (UcdUserTeamMappingsEnum.DEFAULT_TEAM_MAPPINGS_SPECIFIC_TEAMS == defaultTeamMappingType) {
			List<String> teamIds = new ArrayList<String>()
			for (teamName in teamNames) {
				UcdTeam ucdTeam = actionsRunner.runAction([
					action: UcdGetTeam.getSimpleName(),
					team: teamName,
					failIfNotFound: true
				])
				
				String teamId = ucdTeam.getId()
		
				logInfo("Found team [$teamName] ID [$teamId].")
				teamIds.add(teamId)
			}
			requestMap.put("defaultTeamMappings", teamIds)
		}
		
		logInfo("jsonBuilder=$jsonBuilder")
		
		Response response = target.request(MediaType.WILDCARD).put(Entity.json(jsonBuilder.toString()))
		if (response.getStatus() != 200) {
            throw new UcdInvalidValueException(response)
		}
	}	
}
