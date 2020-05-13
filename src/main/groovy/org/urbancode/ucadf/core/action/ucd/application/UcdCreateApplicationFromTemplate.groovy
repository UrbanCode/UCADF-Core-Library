/**
 * This action creates an application template.
 */
package org.urbancode.ucadf.core.action.ucd.application

import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.action.ucd.applicationTemplate.UcdGetApplicationTemplate
import org.urbancode.ucadf.core.action.ucd.component.UcdGetComponent
import org.urbancode.ucadf.core.action.ucd.security.UcdGetSecurityTeamMappings
import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucd.applicationTemplate.UcdApplicationTemplate
import org.urbancode.ucadf.core.model.ucd.component.UcdComponent
import org.urbancode.ucadf.core.model.ucd.general.UcdObject
import org.urbancode.ucadf.core.model.ucd.team.UcdTeamSecurity

import groovy.json.JsonBuilder

class UcdCreateApplicationFromTemplate extends UcAdfAction {
	// Action properties.
	/** The application name. */
	String name

	/** (Optional) The application description. */	
	String description = ""
	
	/** The application template. */
	String applicationTemplate

	/** The application template version. */
	String templateVersion = ""
	
	/** The flag that indicates enforce complete snapshots. Default is false. */
	Boolean enforceCompleteSnapshots = false
	
	/** The list of component names or IDs. */
	List<String> components = []
		
	/** The list of teams/subtypes to add. */
	List<UcdTeamSecurity> teams = []

	/** The flag that indicates fail if the application already exists. Default is true. */
	Boolean failIfExists = true
	
	/**
	 * Runs the action.
	 * @return True if the application template was created.
	 */
	public Boolean run() {
		// Validate the action properties.
		validatePropsExist()

		Boolean created = false
		
		logVerbose("Creating application [$name] from template [$applicationTemplate].")

		// If teams were specified then derive the team mappings.
		List<Map<String, String>> teamMappings = []
		if (teams.size() > 0) {
			teamMappings = actionsRunner.runAction([
				action: UcdGetSecurityTeamMappings.getSimpleName(),
				actionInfo: false,
				teams: teams
			])
		}
		
		// Get the application template information.
		UcdApplicationTemplate ucdApplicationTemplate = actionsRunner.runAction([
			action: UcdGetApplicationTemplate.getSimpleName(),
			actionInfo: false,
			applicationTemplate: applicationTemplate,
			failIfNotFound: true
		])

		// Construct a list of component IDs.
		List<String> componentIds = []
		for (component in components) {
			String componentId = component
			if (!UcdObject.isUUID(component)) {
				UcdComponent ucdComponent = actionsRunner.runAction([
					action: UcdGetComponent.getSimpleName(),
					actionInfo: false,
					component: component,
					failIfNotFound: true
				])
				componentId = ucdComponent.getId()
			}
			componentIds.add(componentId)
		}

		// Initialize the request.
		Map requestMap = [
			name: name,
			description: description,
			enforceCompleteSnapshots: enforceCompleteSnapshots,
			existingComponentIds: componentIds,
			teamMappings: teamMappings,
			templateId: ucdApplicationTemplate.getId(),
			templateProperties: [],
			templateVersion: templateVersion
		]
		logDebug(requestMap.toString())
		
		JsonBuilder jsonBuilder = new JsonBuilder(requestMap)
		
		WebTarget target = ucdSession.getUcdWebTarget().path("/cli/application/createApplicationFromTemplate")
		logDebug("target=$target")

		Response response = target.request(MediaType.APPLICATION_JSON).put(Entity.json(jsonBuilder.toString()))
		if (response.getStatus() == 200) {
			logVerbose("Application [$name] created from template [$applicationTemplate].")
			created = true
		} else {
			String errMsg = UcAdfInvalidValueException.getResponseErrorMessage(response)
			logVerbose(errMsg)
			if (!(response.getStatus() == 400 && (errMsg ==~ /.*already exists.*/ && !failIfExists))) {
				throw new UcAdfInvalidValueException(errMsg)
			}
		}
		
		return created
	}
}
