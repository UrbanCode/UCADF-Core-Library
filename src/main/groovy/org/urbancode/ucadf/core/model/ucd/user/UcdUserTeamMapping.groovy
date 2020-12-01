/**
 * This class instantiates a user team mapping object.
 */
package org.urbancode.ucadf.core.model.ucd.user

import org.urbancode.ucadf.core.model.ucd.team.UcdTeam

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class UcdUserTeamMapping extends UcdTeam {
	UcdUserTeamRoleMapping roleMapping
	Boolean canManage
	
	// Constructors.	
	UcdUserTeamMapping() {
	}
}
