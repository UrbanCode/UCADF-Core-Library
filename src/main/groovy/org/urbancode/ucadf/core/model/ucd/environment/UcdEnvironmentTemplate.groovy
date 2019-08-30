/**
 * This class instantiates environment template objects.
 */
package org.urbancode.ucadf.core.model.ucd.environment

import org.urbancode.ucadf.core.model.ucd.general.UcdSecurityTypeObject

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class UcdEnvironmentTemplate extends UcdSecurityTypeObject {
	// Constructors.
	UcdEnvironmentTemplate() {
	}
}
