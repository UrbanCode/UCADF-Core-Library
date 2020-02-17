/**
 * This class instantiates a version artifact object.
 */
package org.urbancode.ucadf.core.model.ucd.version

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class UcdVersionArtifactStandard extends UcdVersionArtifact {
	/** The child artifacts. */
	List<UcdVersionArtifactStandard> childArtifacts = []
	
	/** The user attributes. */
	List<UcdVersionArtifactUserAttributes> userAttributes
	
	// Constructors.	
	UcdVersionArtifactStandard() {
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
