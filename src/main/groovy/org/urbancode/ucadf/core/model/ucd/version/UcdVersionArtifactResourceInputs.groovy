/**
 * This class instantiates a version artifact resource inputs object.
 */
package org.urbancode.ucadf.core.model.ucd.version

import org.urbancode.ucadf.core.model.ucd.general.UcdObject

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class UcdVersionArtifactResourceInputs extends UcdObject {
	String name
	String compileType
	String version
	URL url
	
	// Constructors.	
	UcdVersionArtifactResourceInputs() {
	}
}
