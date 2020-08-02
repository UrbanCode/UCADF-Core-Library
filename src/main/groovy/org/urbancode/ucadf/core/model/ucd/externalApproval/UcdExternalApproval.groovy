/**
 * This class instantiates external approval objects.
 */
package org.urbancode.ucadf.core.model.ucd.externalApproval

import org.urbancode.ucadf.core.model.ucd.environment.UcdEnvironment

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class UcdExternalApproval extends UcdEnvironment {
	// Constructors.
	UcdExternalApproval() {
	}
}
