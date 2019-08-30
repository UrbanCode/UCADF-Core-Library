/**
 * This is an enumeration of the security type names.
 */
package org.urbancode.ucadf.core.model.ucd.security

import org.urbancode.ucadf.core.model.ucd.agent.UcdAgent
import org.urbancode.ucadf.core.model.ucd.agent.UcdAgentConfigurationTemplate
import org.urbancode.ucadf.core.model.ucd.agentPool.UcdAgentPool
import org.urbancode.ucadf.core.model.ucd.agentRelay.UcdAgentRelay
import org.urbancode.ucadf.core.model.ucd.application.UcdApplication
import org.urbancode.ucadf.core.model.ucd.applicationTemplate.UcdApplicationTemplate
import org.urbancode.ucadf.core.model.ucd.cloud.UcdCloudConnection
import org.urbancode.ucadf.core.model.ucd.component.UcdComponent
import org.urbancode.ucadf.core.model.ucd.componentTemplate.UcdComponentTemplate
import org.urbancode.ucadf.core.model.ucd.environment.UcdEnvironment
import org.urbancode.ucadf.core.model.ucd.environment.UcdEnvironmentTemplate
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.genericProcess.UcdGenericProcess
import org.urbancode.ucadf.core.model.ucd.resource.UcdResource
import org.urbancode.ucadf.core.model.ucd.resource.UcdResourceTemplate
import org.urbancode.ucadf.core.model.ucd.system.UcdServerConfiguration
import org.urbancode.ucadf.core.model.ucd.system.UcdWebUI

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

enum UcdSecurityTypeEnum {
	AGENT("Agent", UcdAgent.class, true),
	AGENTCONFIGURATIONTEMPLATE("Agent Configuration Template", UcdAgentConfigurationTemplate.class, true),
	AGENTPOOL("Agent Pool", UcdAgentPool.class, true),
	AGENTRELAY("Agent Relay", UcdAgentRelay.class, true),
	APPLICATION("Application", UcdApplication.class, true),
	APPLICATIONTEMPLATE("Application Template", UcdApplicationTemplate.class, true),
	CLOUDCONNECTION("Cloud Connection", UcdCloudConnection.class, true),
	COMPONENT("Component", UcdComponent.class, true),
	COMPONENTTEMPLATE("Component Template", UcdComponentTemplate.class, true),
	ENVIRONMENT("Environment", UcdEnvironment.class, true),
	ENVIRONMENTTEMPLATE("Environment Template", UcdEnvironmentTemplate.class, true),
	PROCESS("Process", UcdGenericProcess.class, true),
	RESOURCE("Resource", UcdResource.class, true),
	RESOURCETEMPLATE("Resource Template", UcdResourceTemplate.class, true),
	SERVERCONFIGURATION("Server Configuration", UcdServerConfiguration.class, false),
	WEBUI("Web UI", UcdWebUI.class, false)
	
	private String securityType
	private Class typeClass
	private Boolean subtypeAllowed
	
	// Constructor.	
	UcdSecurityTypeEnum(
		final String securityType,
		final Class typeClass,
		final Boolean subtypeAllowed) {

		this.securityType = securityType
		this.typeClass = typeClass
		this.subtypeAllowed = subtypeAllowed
	}

	/** The enumeration to create for deserialization. Gets a new enumeration by matching either the enumeration name or the security type value. */
	@JsonCreator
	public static UcdSecurityTypeEnum newEnum(final String value) {
		UcdSecurityTypeEnum newEnum = UcdSecurityTypeEnum.values().find {
			(it.name() == value.toUpperCase() || it.getSecurityType() == value)
		}
		
		if (!newEnum) {
			throw new UcdInvalidValueException("Security type enumeration [$value] is invalid.")
		}
		
		return newEnum
	}

	/** Get the security type value. This is the value to use for serialization. */
	@JsonValue	
	public String getSecurityType() {
		return securityType
	}

	/** Get the security type class. */
	public Class getTypeClass() {
		return typeClass
	}

	/** Get the subtype allowed flag. */	
	public Boolean getSubtypeAllowed() {
		return subtypeAllowed
	}
}
