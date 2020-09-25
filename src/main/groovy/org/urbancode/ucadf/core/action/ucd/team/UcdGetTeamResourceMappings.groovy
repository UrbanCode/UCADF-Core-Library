/**
 * This action gets a team's resource mappings.
 */
package org.urbancode.ucadf.core.action.ucd.team

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.GenericType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucd.general.UcdObject
import org.urbancode.ucadf.core.model.ucd.security.UcdSecurityTypeEnum

class UcdGetTeamResourceMappings extends UcAdfAction {
	// Action properties.
	/** The team name or ID. */
	String team
	
	/** The security type. */
	String type
	
	/**
	 * Runs the action.	
	 * @return The list of mappings to entities.
	 */
	@Override
	public List<UcdObject> run() {
		// Validate the action properties.
		validatePropsExist()
		
		List<UcdObject> teamResourceMappings = []
		
		WebTarget target = ucdSession.getUcdWebTarget().path("/rest/security/team/{team}/resourceMappings/{type}")
			.resolveTemplate("team", team)
			.resolveTemplate("type", type)
		logDebug("target=$target")

		// Get the class for the type.	
        Class mapClass = UcdSecurityTypeEnum.newEnum(type).getTypeClass()
		
		Response response = target.request().get()
		if (response.getStatus() == 200) {
			teamResourceMappings = response.readEntity(new GenericType(UcdObject.getParameterizedListGenericType(mapClass)){})
		} else {
			// Later UCD versions started returning a 400 for unknown types.
			String responseMessage = response.readEntity(String.class)
			logDebug(responseMessage)
			if (responseMessage.matches(/.*Unknown.*/)) {
				teamResourceMappings = new ArrayList<UcdObject>()
			} else {
				throw new UcAdfInvalidValueException("Error: ${response.getStatus()}. Unable to get team resource mappings. $target")
			}
		}
		
		return teamResourceMappings
	}
}
