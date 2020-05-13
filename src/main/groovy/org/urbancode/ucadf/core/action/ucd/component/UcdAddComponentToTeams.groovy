/**
 * This action adds a component to teams/subtypes. 
 */
package org.urbancode.ucadf.core.action.ucd.component

import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.action.ucd.security.UcdGetSecuritySubtype
import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.component.UcdComponent
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucd.general.UcdObject
import org.urbancode.ucadf.core.model.ucd.security.UcdSecuritySubtype
import org.urbancode.ucadf.core.model.ucd.security.UcdSecurityTypeEnum
import org.urbancode.ucadf.core.model.ucd.team.UcdTeamSecurity

class UcdAddComponentToTeams extends UcAdfAction {
	// Action properties.
	/** The component name or ID. */
	String component
	
	/** The list of teams/subtypes. */
	List<UcdTeamSecurity> teams
	
	/** If true then any extra teams/subtypes are removed. Default is false. */
	Boolean removeOthers = false
	
	/**
	 * Runs the action.	
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExist()
		
		for (teamSecurity in teams) {
			addComponentToTeam(
				teamSecurity.getTeam(),
				teamSecurity.getSubtype()
			)
		}

		// Remove the component from extra teams.
		if (teams && removeOthers) {
			removeComponentFromExtraTeams(
				teams
			)
		}
	}

	// Add component to a team.
	public addComponentToTeam(
		final String teamName, 
		final String subtype) {
		
		logVerbose("Adding component [$component] to team [$teamName] subtype [$subtype].")

		// Get the subtype name required for the API call.
		String subtypeName = subtype
		if (subtype && UcdObject.isUUID(subtype)) {
			UcdSecuritySubtype ucdSecuritySubtype = actionsRunner.runAction([
				action: UcdGetSecuritySubtype.getSimpleName(),
				actionInfo: false,
				actionVerbose: false,
				subtype: subtype
			])
			
			subtypeName = ucdSecuritySubtype.getName()
		}

        WebTarget target = ucdSession.getUcdWebTarget().path("/cli/component/teams")
			.queryParam("component", component)
			.queryParam("team", teamName)
			.queryParam("type", subtypeName)
			
        logDebug("target=$target")

        Response response = target.request(MediaType.APPLICATION_JSON).put(Entity.json(""))
        if (response.getStatus() == 204) {
            logVerbose("Component added to team.")
        } else {
            throw new UcAdfInvalidValueException(response)
        }
	}

	// Remove the component from extra teams.
	public removeComponentFromExtraTeams(
		final List<UcdTeamSecurity> keepteams) {

		// Get the component information.		
		UcdComponent ucdComponent = actionsRunner.runAction([
			action: UcdGetComponent.getSimpleName(),
			actionInfo: false,
			actionVerbose: false,
			component: component,
			failIfNotFound: true
		])

		for (team in ucdComponent.getExtendedSecurity().getTeams()) {
			Boolean removeTeam = true
			for (keepTeamSecurity in keepteams) {
				if (team.getTeamName() == keepTeamSecurity.getTeam() &&
					((!team.getSubtypeName() && !keepTeamSecurity.getSubtype()) || (team.getSubtypeName() == keepTeamSecurity.getSubtype()))) {
					
					removeTeam = false
					break
				}
			}
			
			if (removeTeam) {
				logVerbose("Removing team [${team.getTeamName()}] role [${team.getSubtypeName()}].")
				
				WebTarget removeTargetWithParams = ucdSession.getUcdWebTarget().path("/cli/component/teams")
					.queryParam("component", component)
					.queryParam("team", team.getTeamName())
					
				if (team.getSubtypeName()) {
					removeTargetWithParams = removeTargetWithParams.queryParam("type", team.getSubtypeName())
				} else {
					removeTargetWithParams = removeTargetWithParams.queryParam("type", "")
				}
				logDebug("removeTargetWithParams=$removeTargetWithParams")
				
				Response response = removeTargetWithParams.request(MediaType.APPLICATION_JSON).delete()
				if (response.getStatus() != 204) {
					throw new UcAdfInvalidValueException(response)
				}
			}
		}
	}
}
