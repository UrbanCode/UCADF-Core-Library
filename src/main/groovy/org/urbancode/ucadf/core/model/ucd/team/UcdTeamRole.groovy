/**
 * This class is used to define a team name and role combination.
 */
package org.urbancode.ucadf.core.model.ucd.team

import org.urbancode.ucadf.core.model.ucd.general.UcdObject

class UcdTeamRole extends UcdObject {
	String team = ""
	String role = ""
	
	UcdTeamRole() {
	}
	
	UcdTeamRole(
		final String team, 
		final String role) {
		
		this.team = team
		this.role = role
	}
}
