/**
 * This class is used to provide team/subtype information to actions requiring that information.
 */
package org.urbancode.ucadf.core.model.ucd.team

import org.urbancode.ucadf.core.model.ucd.general.UcdObject

class UcdTeamSecurity extends UcdObject {
	/** The team name or ID. */
	String team = ""
	
	/** The subtype name or ID. */
	String subtype = ""

	// Constructors.
	UcdTeamSecurity() {
	}
	
	UcdTeamSecurity(
		final String team,
		final String subtype) {
		
		this.team = team
		this.subtype = subtype
	}
}
