/**
 * This class instantiates a server configuration object.
 */
package org.urbancode.ucadf.core.model.ucd.system

import org.urbancode.ucadf.core.model.ucd.general.UcdSecurityTypeObject

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class UcdServerConfiguration extends UcdSecurityTypeObject {
	// Constructors.	
	UcdServerConfiguration() {
	}
}
