/**
 * This class instantiates extended security type objects.
 */
package org.urbancode.ucadf.core.model.ucd.security

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class UcdExtendedSecurity extends UcdSecurityPermissionProperties {
	/** The list of extended security teams. */
	List<UcdExtendedSecurityTeam> teams

	// Constructors.	
	UcdExtendedSecurity() {
	}
}
