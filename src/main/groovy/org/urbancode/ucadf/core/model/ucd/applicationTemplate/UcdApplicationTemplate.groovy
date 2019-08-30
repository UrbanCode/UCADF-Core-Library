/**
 * This class instantiates application template objects.
 */
package org.urbancode.ucadf.core.model.ucd.applicationTemplate

import org.urbancode.ucadf.core.model.ucd.general.UcdSecurityTypeObject

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class UcdApplicationTemplate extends UcdSecurityTypeObject {
	// Constructors.	
	UcdApplicationTemplate() {
	}
}
