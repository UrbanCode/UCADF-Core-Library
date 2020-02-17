/**
 * This enumeration represents the version artifact deploy action values.
 */
package org.urbancode.ucadf.core.model.ucd.version

import com.fasterxml.jackson.annotation.JsonValue

enum UcdVersionArtifactDeployActionEnum {
	/** The add action. */
	ADD("ADD"),
	
	/** The delete action. */
	DELETE("DELETE")
	
	private String deployAction
	
	// Constructor.	
	UcdVersionArtifactDeployActionEnum(final String deployAction) {
		this.deployAction = deployAction
	}

	@JsonValue	
	public String getDeployAction() {
		return deployAction
	}
}
