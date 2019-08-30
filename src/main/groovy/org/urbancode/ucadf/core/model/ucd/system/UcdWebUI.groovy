/**
 * This class instantiates the web UI security object.
 */
package org.urbancode.ucadf.core.model.ucd.system

import org.urbancode.ucadf.core.model.ucd.general.UcdSecurityTypeObject

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class UcdWebUI extends UcdSecurityTypeObject {
	// Constructors.	
	UcdWebUI() {
	}
}
