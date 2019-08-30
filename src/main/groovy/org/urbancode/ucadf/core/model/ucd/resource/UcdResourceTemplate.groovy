/**
 * This class instantiates resource template objects.
 */
package org.urbancode.ucadf.core.model.ucd.resource

import org.urbancode.ucadf.core.model.ucd.general.UcdSecurityTypeObject

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

// Used for getTeamResourceMappings
@JsonIgnoreProperties(ignoreUnknown = true)
class UcdResourceTemplate extends UcdSecurityTypeObject {
	// Constructors.
	UcdResourceTemplate() {
	}
}
