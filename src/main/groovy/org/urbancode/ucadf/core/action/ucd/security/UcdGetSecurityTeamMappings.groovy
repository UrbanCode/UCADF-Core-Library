/**
 * This action is used internally to get a merged set of team mappings in the format acceptable to the REST APIs.
 */
package org.urbancode.ucadf.core.action.ucd.security

import org.urbancode.ucadf.core.action.ucd.team.UcdGetTeam
import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.general.UcdObject
import org.urbancode.ucadf.core.model.ucd.security.UcdExtendedSecurity
import org.urbancode.ucadf.core.model.ucd.security.UcdSecuritySubtype
import org.urbancode.ucadf.core.model.ucd.team.UcdTeam
import org.urbancode.ucadf.core.model.ucd.team.UcdTeamSecurity

class UcdGetSecurityTeamMappings extends UcAdfAction {
	/** The extended security from the existing object. This will be used for the initial map unless removeOthers is specified. */
	UcdExtendedSecurity extendedSecurity
	
	/** The list of teams/subtypes. If not specified and removeOthers is true then all teams are removed. */
	List<UcdTeamSecurity> teams = []
	
	/** If true then any extra teams/subtypes are removed and the initial extendedSecurity is ignored. Default is false. */
	Boolean removeOthers = false
		
	/**
	 * Runs the action.	
	 * @return The team mappings.
	 */
	@Override
	public List<Map<String, String>> run() {
		// Validate the action properties.
		validatePropsExist()

		// Get the team mappings.			
		List<Map<String, String>> teamMappings = []

		// If not removing others then initialize the map with the current teams/subtypes.
		if (extendedSecurity.getTeams() && !removeOthers) {
			for (ucdExtendedSecurityTeam in extendedSecurity.getTeams()) {
				String teamId = ucdExtendedSecurityTeam.getTeamId()
				String subtypeId = ucdExtendedSecurityTeam.getSubtypeId()
			
				if (subtypeId) {
					teamMappings.add([ teamId: teamId, resourceRoleId: subtypeId ])
				} else {
					teamMappings.add([ teamId: teamId ])
				}
			}
		}

		// Add the requested teams/subtypes to the map if they aren't already in it.
		for (ucdTeamSecurity in teams) {
			String team = ucdTeamSecurity.getTeam()
			String subtype = ucdTeamSecurity.getSubtype()

			// If the team is a UUID then use it, otherwise look up the team.
			String teamId
			if (UcdObject.isUUID(team)) {
				teamId = team
			} else {
				UcdTeam ucdTeam = actionsRunner.runAction([
					action: UcdGetTeam.getSimpleName(),
					actionInfo: false,
					team: team,
					failIfNotFound: true
				])

				teamId = ucdTeam.getId()
			}

			// If a subtype was specified then get the subtype ID. */
			String subtypeId
			if (subtype) {
				UcdSecuritySubtype ucdSecuritySubtype = actionsRunner.runAction([
					action: UcdGetSecuritySubtype.getSimpleName(),
					actionInfo: false,
					actionVerbose: true,
					subtype: subtype,
					failIfNotFound: true
				])
				
				subtypeId = ucdSecuritySubtype.getId()
			}
			
			// Determine if the entry already exists in the map.
			Map<String, String> requestMapping = teamMappings.find { mappedTeam ->
				String mappedTeamId = mappedTeam['teamId']
				String mappedTeamSubtypeId = mappedTeam['resourceRoleId']
				
				if (mappedTeamId == teamId &&
					((!mappedTeamSubtypeId && !subtype) || (mappedTeamSubtypeId == subtype))) {
				}
			}
			
			if (!requestMapping) {
				if (subtypeId) {
					teamMappings.add([ teamId: teamId, resourceRoleId: subtypeId ])
				} else {
					teamMappings.add([ teamId: teamId ])
				}
			}
		}
		
		return teamMappings
	}
}
