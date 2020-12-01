/**
 * This class instantiates a user team role mapping object.
 */
package org.urbancode.ucadf.core.model.ucd.user

import org.urbancode.ucadf.core.model.ucd.general.UcdObject
import org.urbancode.ucadf.core.model.ucd.role.UcdRole

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class UcdUserTeamRoleMapping extends UcdObject {
	UcdRole role
	Object context
	
	// Constructors.	
	UcdUserTeamRoleMapping() {
	}
}
