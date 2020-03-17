/**
 * This class instantiates a version artifact object.
 */
package org.urbancode.ucadf.core.model.ucd.version

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class UcdVersionArtifactZOS extends UcdVersionArtifact {
	/** The Z/OS deploy action. */
	UcdVersionArtifactDeployActionEnum zosDeployAction
	
	/** The Z/OS container type. */
	UcdVersionArtifactContainerTypeEnum zosContainerType
	
	/** The input names. */
	String inputNames

	// Constructors.	
	UcdVersionArtifactZOS() {
	}
}
