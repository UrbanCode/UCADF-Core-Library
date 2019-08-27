/**
 * This class instantiates a UCD server object.
 */
package org.urbancode.ucadf.core.model.ucd.system

import org.urbancode.ucadf.core.model.ucd.general.UcdObject

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class UcdServer extends UcdObject {
	// Constructors.	
	UcdServer() {
	}
}
