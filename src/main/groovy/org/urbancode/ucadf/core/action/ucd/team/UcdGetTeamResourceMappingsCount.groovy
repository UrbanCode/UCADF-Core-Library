/**
 * This action gets the count of a team's mappings to entities.
 */
package org.urbancode.ucadf.core.action.ucd.team

import org.urbancode.ucadf.core.action.ucd.security.UcdGetSecurityTypes
import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.general.UcdObject
import org.urbancode.ucadf.core.model.ucd.security.UcdSecurityType
import org.urbancode.ucadf.core.model.ucd.team.UcdTeam

class UcdGetTeamResourceMappingsCount extends UcAdfAction {
	// Action properties.
	/** The team name or ID. */
	String team
	
	/** (Optional) The regular expression used to exclude types. */
	String regexExclude = ""
	
	/** (Optional) The list of security type names or IDs to include. */
	List<String> types = []
	
	/** The flag that indicates fail if the team is not found. Default is true. */
	Boolean failIfNotFound = true
	
	/**
	 * Runs the action.	
	 * @return The count of mappings to entities.
	 */
	@Override
	public Long run() {
		// Validate the action properties.
		validatePropsExist()
		
		Long count = 0
		
		// If no types were specified then use all of the ones from the system.
		if (types.size() == 0) {
			// Get the list of security types.
			List<UcdSecurityType> ucdSecurityTypes = actionsRunner.runAction([
				action: UcdGetSecurityTypes.getSimpleName()
			])
			
			for (ucdSecurityType in ucdSecurityTypes) {
				types.add(ucdSecurityType.getName())
			}
		}

		UcdTeam ucdTeam = actionsRunner.runAction([
			action: UcdGetTeam.getSimpleName(),
			team: team,
			failIfNotFound: failIfNotFound
		])

		if (ucdTeam) {
			for (type in types) {
				if (type ==~ /$regexExclude/) {
					continue
				}

				List<UcdObject> teamResourceMappings = actionsRunner.runAction([
					action: UcdGetTeamResourceMappings.getSimpleName(),
					team: ucdTeam.getId(),
					type: type
				])
				
	            count += teamResourceMappings.size()
			}
		}

		return count
	}
}
