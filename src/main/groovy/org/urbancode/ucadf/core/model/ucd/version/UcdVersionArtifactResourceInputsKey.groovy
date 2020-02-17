/**
 * This class instantiates a version artifact resource inputs key object.
 */
package org.urbancode.ucadf.core.model.ucd.version

import org.urbancode.ucadf.core.model.ucd.general.UcdObject

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class UcdVersionArtifactResourceInputsKey extends UcdObject {
	String url
	List<UcdVersionArtifactResourceInputs> zOSResourceInputs
	
	// Constructors.	
	UcdVersionArtifactResourceInputsKey() {
	}
}
