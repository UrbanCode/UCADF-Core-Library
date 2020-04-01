/**
 * This class instantiates agent relay objects.
 */
package org.urbancode.ucadf.core.model.ucd.agentRelay

import org.urbancode.ucadf.core.model.ucd.general.UcdSecurityTypeObject
import org.urbancode.ucadf.core.model.ucd.security.UcdExtendedSecurity

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class UcdAgentRelay extends UcdSecurityTypeObject {
	/** The agent relay ID. */
	String id
	
	/** The name. */
	String name
	
	/** The description. */
	String description
	
	/** The endpoint ID. */
	String endpointId
	
	/** The version. */
	String version
	
	/** The host name. */
	String hostname
	
	/** The relay host name. */
	String relayHostname
	
	/** The JMS port number. */
	Long jmsPort

	/** The status. */
	UcdAgentRelayStatusEnum status
	
	/** The total agents. */
	Long totalAgents
	
	/** The active agents. */
	Long activeAgents
	
	/** The last contact date. */
	Long lastContact
	
	/** The extended security. */
	UcdExtendedSecurity extendedSecurity

	/** The security resource ID. */
	String securityResourceId
	
	/** The communication version. */
	String communicationVersion
	
	/** TODO: What is this? */
	Object security
		
	// Constructors.	
	UcdAgentRelay() {
	}
}
