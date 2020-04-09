/**
 * This action adds an application template to teams/subtypes.
 */
package org.urbancode.ucadf.core.action.ucd.applicationTemplate

import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.action.ucd.security.UcdGetSecurityTeamMappings
import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucd.applicationTemplate.UcdApplicationTemplate
import org.urbancode.ucadf.core.model.ucd.applicationTemplate.UcdApplicationTemplateTagRequirementRequest
import org.urbancode.ucadf.core.model.ucd.team.UcdTeamSecurity

import groovy.json.JsonBuilder

class UcdAddApplicationTemplateToTeams extends UcAdfAction {
	// Action properties.
	/** The application template name or ID. */
	String applicationTemplate
	
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

		UcdApplicationTemplate ucdApplicationTemplate = actionsRunner.runAction([
			action: UcdGetApplicationTemplate.getSimpleName(),
			actionInfo: false,
			applicationTemplate: applicationTemplate,
			failIfNotFound: true
		])

		List<UcdApplicationTemplateTagRequirementRequest> tagRequirements = []
		for (ucdTagRequirement in ucdApplicationTemplate.getTagRequirements()) {
			tagRequirements.add(
				new UcdApplicationTemplateTagRequirementRequest(
					ucdTagRequirement.getTag().getName(), 
					ucdTagRequirement.getType(), 
					ucdTagRequirement.getNumber()
				)
			)
		}
		
		// Get the team mappings.
		List<Map<String, String>> teamMappings = actionsRunner.runAction([
			action: UcdGetSecurityTeamMappings.getSimpleName(),
			actionInfo: false,
			extendedSecurity: ucdApplicationTemplate.getExtendedSecurity(),
			teams: teams,
			removeOthers: removeOthers
		])
		
		Map requestMap = [
			name: ucdApplicationTemplate.getName(),
			description: ucdApplicationTemplate.getDescription(),
			notificationSchemeId: ucdApplicationTemplate.getNotificationSchemeId(),
			enforceCompleteSnapshots: ucdApplicationTemplate.getEnforceCompleteSnapshots(),
			tagRequirements: tagRequirements,
			existingId: ucdApplicationTemplate.getId(),
			teamMappings: teamMappings
		]
		
		JsonBuilder jsonBuilder = new JsonBuilder(requestMap)
		logDebug("jsonBuilder=$jsonBuilder")
		
		WebTarget target = ucdSession.getUcdWebTarget().path("/rest/deploy/applicationTemplate")
		logDebug("target=$target")
		
		Response response = target.request(MediaType.APPLICATION_JSON).put(Entity.json(jsonBuilder.toString()))
		if (response.getStatus() != 200) {	
			throw new UcAdfInvalidValueException(response)
		}
    }
}
