/**
 * This action sets the user preferences default team mappings.
 */
package org.urbancode.ucadf.core.action.ucd.user

import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.GenericType
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucd.user.UcdUserTeamMappingsEnum

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

import groovy.json.JsonBuilder

// Set the user team preferences.
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
			throw new UcAdfInvalidValueException("This action requires a value for ucdUserId and ucdUserPw, not ucdAuthToken.")
		}
		
		logVerbose("Set user team preferences type [$defaultTeamMappingType] teamNames [$teamNames].")
		
		WebTarget target
		
		// Build a custom body that includes only the required fields.
		Map requestMap = [
			defaultTeamMappingType: defaultTeamMappingType
		]
		JsonBuilder jsonBuilder = new JsonBuilder(requestMap)

		if (UcdUserTeamMappingsEnum.DEFAULT_TEAM_MAPPINGS_SPECIFIC_TEAMS == defaultTeamMappingType) {
			// Get the list of groups the user belongs to. Have to do this because the user might not have manage security privileges to use UcdGetTeam.
			List<UserGroup> userGroups
			
			target = ucdSession.getUcdWebTarget().path("/security/team")
				.queryParam("filterFields", "username")
				.queryParam("filterValue_username", ucdUserId)
				.queryParam("filterType_username", "eq")
				.queryParam("filterClass_username", "String&name=*")
			logDebug("target=$target")
			
			Response response = target.request().get()
			if (response.getStatus() == 200) {
				userGroups = target.request().get(new GenericType<List<UserGroup>>(){})
			} else {
				throw new UcAdfInvalidValueException(response)
			}
		
			List<String> teamIds = []
			for (teamName in teamNames) {
				UserGroup userTeam = userGroups.find {
					it.getName().equals(teamName)
				}
				
				if (!userTeam) {
					throw new UcAdfInvalidValueException("User [$ucdUserId] is not a member of team [$teamName].")
				}
				
				String teamId = userTeam.getId()
		
				logVerbose("Found team [$teamName] ID [$teamId].")
				teamIds.add(teamId)
			}
			requestMap.put("defaultTeamMappings", teamIds)
		}
		
		logVerbose("jsonBuilder=$jsonBuilder")
		
		target = ucdSession.getUcdWebTarget().path("/rest/security/userPreferences")
		logDebug("target=$target")
		
		Response response = target.request(MediaType.WILDCARD).put(Entity.json(jsonBuilder.toString()))
		if (response.getStatus() != 200) {
            throw new UcAdfInvalidValueException(response)
		}
	}	
}

// Used just for getting the available team IDs for setting the user preferences.
@JsonIgnoreProperties(ignoreUnknown = true)
class UserGroup {
	String id
	String name
	String description
	Boolean isDeletable
}
