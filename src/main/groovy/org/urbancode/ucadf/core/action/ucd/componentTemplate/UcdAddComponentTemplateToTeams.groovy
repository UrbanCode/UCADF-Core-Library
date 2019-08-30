/**
 * This action adds a component template to teams/subtypes.
 */
package org.urbancode.ucadf.core.action.ucd.componentTemplate

import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.action.ucd.security.UcdGetSecuritySubtype
import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.general.UcdObject
import org.urbancode.ucadf.core.model.ucd.security.UcdSecuritySubtype
import org.urbancode.ucadf.core.model.ucd.security.UcdSecurityTypeEnum
import org.urbancode.ucadf.core.model.ucd.team.UcdTeamSecurity

class UcdAddComponentTemplateToTeams extends UcAdfAction {
	// Action properties.
	/** The component template name or ID. */
	String componentTemplate
	
	/** The list of teams/subtypes. */
	List<UcdTeamSecurity> teams
	
	/**
	 * Runs the action.	
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExist()

		for (teamSecurity in teams) {
			String team = teamSecurity.getTeam()
			String subtype = teamSecurity.getSubtype()
			
			logInfo("Adding component template [$componentTemplate] to team [$team]subtype [$subtype].")
			
			// Get the subtype ID.
			String subtypeId = subtype
			if (subtype && !UcdObject.isUUID(subtype)) {
				UcdSecuritySubtype ucdSecuritySubtype = actionsRunner.runAction([
					action: UcdGetSecuritySubtype.getSimpleName(),
					type: UcdSecurityTypeEnum.COMPONENTTEMPLATE,
					subtype: subtype
				])
				
				subtypeId = ucdSecuritySubtype.getId()
			}

	        WebTarget target = ucdSession.getUcdWebTarget().path("/cli/componentTemplate/teams")
				.queryParam("componentTemplate", componentTemplate)
				.queryParam("team", team)
				.queryParam("type", subtypeId)
	        logDebug("target=$target")
	
	        Response response = target.request(MediaType.APPLICATION_JSON).put(Entity.json(""))
	        if (response.getStatus() == 204) {
	            logInfo("Component template [$componentTemplate] added to team [$team].")
	        } else {
				throw new UcdInvalidValueException(response)
	        }
		}
    }
}
