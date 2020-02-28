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
	
	/** The child artifacts. */
	List<UcdVersionArtifactZOS> childArtifacts = []
	
	/** The user attributes. */
	UcdVersionArtifactUserAttributes userAttributes

	/** The input names. */
	String inputNames

	// Constructors.	
	UcdVersionArtifactZOS() {
	}
	
	@Override
	public List<UcdVersionArtifact> getChildArtifacts() {
		return childArtifacts
	}
	
	@Override
	public setChildArtifacts(List<UcdVersionArtifact> childArtifacts) {
		this.childArtifacts = childArtifacts
	}
}