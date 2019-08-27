/**
 * This class instantiates extended security objects.
 */
package org.urbancode.ucadf.core.model.ucd.security

import org.urbancode.ucadf.core.model.ucd.general.UcdObject

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
class UcdExtendedSecurityTeam extends UcdObject {
	/** The team ID. */
	String teamId
	
	/** The team name. (Known to the UCD APIs as teamLabel. */	
	@JsonProperty("teamLabel")
	String teamName

	/** The security type ID. (Known to the UCD APIs as resourceTypeId. */	
	@JsonProperty("resourceTypeId")
	String typeId
	
	/** The security type name. (Known to the UCD APIs as resourceTypeName. */	
	@JsonProperty("resourceTypeName")
	String typeName
	
	/** The security subtype ID. (Known to the UCD APIs as resourceRoleId. */	
	@JsonProperty("resourceRoleId")
	String subtypeId
	
	/** The security subtype name. (Known to the UCD APIs as resourceRoleLabel. */	
	@JsonProperty("resourceRoleLabel")
	String subtypeName

	// Constructors.	
	UcdExtendedSecurityTeam() {
	}
	
	UcdExtendedSecurityTeam(
		final String team,
		final String type,
		final String subtype) {
		
		this.teamName = team
		this.typeName = type
		this.subtypeName = subtype
	}
}
