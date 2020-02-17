/**
 * This action gets a list of agent properties.
 */
package org.urbancode.ucadf.core.action.ucd.agent

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.agent.UcdAgent
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.general.UcdObject
import org.urbancode.ucadf.core.model.ucd.property.UcdPropSheet
import org.urbancode.ucadf.core.model.ucd.property.UcdProperty

class UcdGetAgentProperties extends UcAdfAction {
	/** The type of collection to return. */
	enum ReturnAsEnum {
		/** Return as a list. */
		LIST,
		
		/** Return as a map having the property name as the key. */
		MAPBYNAME
	}

	// Action properties.
	// Action properties.
	/** The agent name or ID. */
	String agent
	
	/** The type of collection to return. */
	ReturnAsEnum returnAs = ReturnAsEnum.MAPBYNAME
	
	/** The flag that indicates fail if the agent is not found. Default is true. */
	Boolean failIfNotFound = true

	/**
	 * Runs the action.
	 * @return The specified type of collection.
	 */
	public Object run() {
		// Validate the action properties.
		validatePropsExist()

		Object agentProperties
		
		// If an application ID was provided then use it. Otherwise get the application information to get the ID.
		String agentId = agent
		if (!UcdObject.isUUID(agent)) {
			UcdAgent ucdApplication = actionsRunner.runAction([
				action: UcdGetAgent.getSimpleName(),
				actionInfo: false,
				agent: agent,
				failIfNotFound: failIfNotFound
			])
			agentId = ucdApplication.getId()
		}

		WebTarget target = ucdSession.getUcdWebTarget().path("/property/propSheet/agents&{agentId}&propSheet.-1")
			.resolveTemplate("agentId", agentId)
		logDebug("target=$target")
		
		Response response = target.request().get()
		if (response.getStatus() == 200) {
			UcdPropSheet ucdPropSheet = response.readEntity(UcdPropSheet.class)
			if (ReturnAsEnum.LIST.equals(returnAs)) {
				agentProperties = ucdPropSheet.getProperties()
			} else {
				Map<String, UcdProperty> propertiesMap = [:]
				for (agentProperty in ucdPropSheet.getProperties()) {
					propertiesMap.put(agentProperty.getName(), agentProperty)
				}
				agentProperties = propertiesMap
			}
		} else {
            throw new UcdInvalidValueException(response)
		}
		
		return agentProperties
	}	
}
