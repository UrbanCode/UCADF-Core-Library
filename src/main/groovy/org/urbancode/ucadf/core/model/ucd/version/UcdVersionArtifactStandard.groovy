/**
 * This class instantiates a version artifact object.
 */
package org.urbancode.ucadf.core.model.ucd.version

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class UcdVersionArtifactStandard extends UcdVersionArtifact {
	// Constructors.	
	UcdVersionArtifactStandard() {
	}
}
