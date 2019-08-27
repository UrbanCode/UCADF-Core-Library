/**
 * This action adds resources to teams/subtypes.
 */
package org.urbancode.ucadf.core.action.ucd.resource

import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.action.ucd.security.UcdGetSecurityTeamMappings
import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.resource.UcdResource
import org.urbancode.ucadf.core.model.ucd.team.UcdTeamSecurity

import groovy.json.JsonBuilder

class UcdAddResourcesToTeams extends UcAdfAction {
	/** The list of resource paths or IDs. */
	List<String> resources
	
	/** The list of teams/subtypes. If not specified and removeOthers is true then all teams are removed. */
	List<UcdTeamSecurity> teams = []
	
	/** If true then any extra teams/subtypes are removed. Default is false. */
	Boolean removeOthers = false

	/**
	 * Runs the action.	
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExist()

		// Process each specified resource.
		for (resource in resources) {
			// The resource to update.
			UcdResource ucdResource = actionsRunner.runAction([
				action: UcdGetResource.getSimpleName(),
				actionInfo: false,
				resource: resource,
				failIfNotFound: true
			])

			// Get the team mappings.
			List<Map<String, String>> teamMappings = actionsRunner.runAction([
				action: UcdGetSecurityTeamMappings.getSimpleName(),
				actionInfo: false,
				extendedSecurity: ucdResource.getExtendedSecurity(),
				teams: teams,
				removeOthers: removeOthers
			])

			// Update the team mappings.
			WebTarget target = ucdSession.getUcdWebTarget().path("/rest/resource/resource")
			logDebug("target=$target")
	
			// The payload was validated with 7.1.0.2.
			Map data = [
				name: ucdResource.getName(),
				dynamic: false,											// TODO: Don't see a way to get this property so defaulted.
				description: ucdResource.getDescription(),
				inheritTeam: false,
				useImpersonation: ucdResource.getImpersonationForce(),	// TODO: Not sure about this since not tested with impersonation.
				existingId: ucdResource.getId(),
				parentId: ucdResource.getParent()?.getId(),				// Use Groovy safe navigation ? operator in case parent is null.
				teamMappings: teamMappings
			]
			
			JsonBuilder jsonBuilder = new JsonBuilder(data)
			logDebug(jsonBuilder.toPrettyString())
	
			Response response = target.request(MediaType.APPLICATION_JSON).put(Entity.json(jsonBuilder.toString()))
			if (response.getStatus() != 204 && response.status != 200) {
				throw new UcdInvalidValueException(response)
			}
		}
	}
}
