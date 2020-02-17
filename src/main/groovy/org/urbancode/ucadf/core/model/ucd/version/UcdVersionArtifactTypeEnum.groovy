/**
 * This enumeration represents the version artifact type values.
 */
package org.urbancode.ucadf.core.model.ucd.version

import com.fasterxml.jackson.annotation.JsonValue

enum UcdVersionArtifactTypeEnum {
	/** The type is a partined data set. */
	FOLDER("folder"),
	
	/** The type is a file. */
	FILE("file")

	private String artifactType
	
	// Constructor.	
	UcdVersionArtifactTypeEnum(final String artifactType) {
		this.artifactType = artifactType
	}

	@JsonValue	
	public String getArtifactType() {
		return artifactType
	}
}
