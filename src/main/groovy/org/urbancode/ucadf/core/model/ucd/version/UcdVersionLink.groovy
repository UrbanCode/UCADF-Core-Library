/**
 * This class instantiates a version link object.
 */
package org.urbancode.ucadf.core.model.ucd.version

import org.urbancode.ucadf.core.model.ucd.general.UcdObject

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class UcdVersionLink extends UcdObject {
	/** The link name. */
	String name
	
	/** The link value. */
	String value
	
	// Constructors.	
	UcdVersionLink() {
	}
}
