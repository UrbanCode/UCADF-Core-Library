/**
 * This action removes agents that have been offline for a specified number of minutes and are not associated with a resource.
 */
package org.urbancode.ucadf.core.action.ucd.agent

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.GenericType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.agent.UcdAgent
import org.urbancode.ucadf.core.model.ucd.agent.UcdAgentStatusEnum
import org.urbancode.ucadf.core.model.ucd.filterField.UcdFilterField
import org.urbancode.ucadf.core.model.ucd.filterField.UcdFilterFieldClassEnum
import org.urbancode.ucadf.core.model.ucd.filterField.UcdFilterFieldTypeEnum
import org.urbancode.ucadf.core.model.ucd.resource.UcdResource

class UcdDeleteUnusedAgents extends UcAdfAction {
	// Action properties.
	/** The number of minutes since the last agent contact. */
	Long lastContactMinutesAgo

	/**
	 * Runs the action.	
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExist()

		// Get the current timestamp in milliseconds.
		Date currentDate = new Date()
		Long currentTimeStamp = currentDate.getTime()
		println "currentDate=${currentDate} currentTimeStamp=$currentTimeStamp"

		// Derive the last contact timestamp in milliseconds.
		Date lastContactDate = new Date(currentTimeStamp - (lastContactMinutesAgo * 60 * 1000))		
		Long lastContactTimeStamp = lastContactDate.getTime()
		println "lastContactDate=${lastContactDate} lastContactTimeStamp=$lastContactTimeStamp"
		
		// Construct the query fields.
		List<UcdFilterField> filterFields = [
			new UcdFilterField(
				"lastContact", 
				lastContactTimeStamp.toString(), 
				UcdFilterFieldTypeEnum.lt, 
				UcdFilterFieldClassEnum.Long
			),
			
			new UcdFilterField(
				"status",
				UcdAgentStatusEnum.OFFLINE.toString(),
				UcdFilterFieldTypeEnum.eq,
				UcdFilterFieldClassEnum.Enum
			)
		]

		WebTarget target = UcdFilterField.addFilterFieldQueryParams(
			ucdSession.getUcdWebTarget().path("/rest/agent"), 
			filterFields
		)
		
		logDebug("target=$target")

		List<UcdAgent> ucdAgents = []
		
		Response response 
		
		response = target.request().get()
		if (response.getStatus() == 200) {
			ucdAgents = response.readEntity(new GenericType<List<UcdAgent>>(){})
		} else {
			logVerbose(response.readEntity(String.class))
			throw new Exception("Status: ${response.getStatus()} Unable to get list of agents. $target")
		}

		logVerbose("Found [${ucdAgents.size()}] agents that haven't been contacted since [$lastContactDate]."		)

		// Evaluate each agent and delete if possible.
		for (ucdAgent in ucdAgents) {
			Date lastAgentContactDate = new Date(ucdAgent.getLastContact())
			logVerbose("Evaluating agent [${ucdAgent.getName()}] last contact [$lastAgentContactDate] created [${ucdAgent.getDateCreated()}].")
			
			target = ucdSession.getUcdWebTarget().path("/rest/agent/{agentId}/resources")
				.resolveTemplate("agentId", ucdAgent.getId())
			logDebug("target=$target")

			List<UcdResource> ucdResources = []
					
			response = target.request().get()
			if (response.getStatus() == 200) {
				ucdResources = response.readEntity(new GenericType<List<UcdResource>>(){})
			} else {
				logVerbose(response.readEntity(String.class))
				throw new Exception("Status: ${response.getStatus()} Unable to get agent resources. $target")
			}

			if (ucdResources.size() > 0) {			
				logVerbose("Skipping agent [${ucdAgent.getName()}] associated with [${ucdResources.size()}] resources.")
				continue
			}

			// Delete the agent.
			actionsRunner.runAction([
				action: UcdDeleteAgent.getSimpleName(),
				actionInfo: false,
				agent: ucdAgent.getName()
			])
		}
	}
}
