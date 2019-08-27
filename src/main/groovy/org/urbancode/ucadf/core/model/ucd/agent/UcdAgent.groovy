/**
 * This class instantiates agent objects.
 */
package org.urbancode.ucadf.core.model.ucd.agent

import org.urbancode.ucadf.core.model.ucd.general.UcdSecurityTypeObject
import org.urbancode.ucadf.core.model.ucd.property.UcdPropSheet
import org.urbancode.ucadf.core.model.ucd.security.UcdExtendedSecurity
import org.urbancode.ucadf.core.model.ucd.security.UcdSecurityPermissionProperties
import org.urbancode.ucadf.core.model.ucd.tag.UcdTag

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class UcdAgent extends UcdSecurityTypeObject {
	/** The agent ID. */
	String id
	
	/** The agent name. */
	String name
	
	/** The description. */
	String description
	
	/** The flag that indicates if the agent is active. */
	Boolean active
	
	/** The flag that indicates if the agent is licensed. */
	Boolean licensed
	
	/** The license type. */
	String licenseType
	
	/** The status. */
	UcdAgentStatusEnum status
	
	/** The version. */
	String version
	
	/** The working directory. */
	String workingDirectory
	
	/** The impersonation password. */
	String impersonationPassword
	
	/** The flag that indicates to use sudo impersonation. */
	Boolean impersonationUseSudo
	
	/** The flag that indicates force impersonation. */
	Boolean impersonationForce
	
	/** The tags on the agent. */
	List<UcdTag> tags
	
	/** The property sheet. */
	UcdPropSheet propSheet
	
	/** The last contact date. */
	Long lastContact
	
	/** The date created. */
	Date dateCreated
	
	/** The relay ID. */
	String relayId
	
	/** The endpoint ID. */
	String endpointId
	
	/** The communication version. */
	String communicationVersion

	/** The security resource ID. */
	String securityResourceId
	
	/** The security properties. */
	UcdSecurityPermissionProperties security
	
	/** The extended security. */
	UcdExtendedSecurity extendedSecurity
	
	// Constructors.
	UcdAgent() {
	}

	/**
	 * Determine if the agent status is an online-type status.
	 * @return Returns true if the agent has an online-type status.
	 */
	@JsonIgnore
	public Boolean hasOnlineStatus() {	
		return (status == UcdAgentStatusEnum.ONLINE || status == UcdAgentStatusEnum.UPGRADE) ? true : false
	}
}
