/**
 * This class instantiates agent pool objects.
 */
package org.urbancode.ucadf.core.model.ucd.agentPool

import org.urbancode.ucadf.core.model.ucd.agent.UcdAgent
import org.urbancode.ucadf.core.model.ucd.general.UcdSecurityTypeObject
import org.urbancode.ucadf.core.model.ucd.security.UcdExtendedSecurity
import org.urbancode.ucadf.core.model.ucd.security.UcdSecurityPermissionProperties

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class UcdAgentPool extends UcdSecurityTypeObject {
	/** The pool ID. */
	String id
	
	/** The name. */
	String name
	
	/** The description. */
	String description
	
	/** The list of associated agent IDs. */
	List<String> agentIds
	
	/** The list of associated agent objects. */
	List<UcdAgent> agents
	
	/** The flag that indicates the agent pool is active. */
	Boolean active

	/** The security resource ID. */
	String securityResourceId

	/** The security properties. */
	UcdSecurityPermissionProperties security
	
	/** The extended security. */
	UcdExtendedSecurity extendedSecurity

	/** The agent pool status. */
	UcdAgentPoolStatusEnum status

	// Constructors.	
	UcdAgentPool() {
	}
}
