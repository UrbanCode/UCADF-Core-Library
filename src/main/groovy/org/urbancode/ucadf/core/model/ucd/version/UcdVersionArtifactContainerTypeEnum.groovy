/**
 * This enumeration represents the version artifact container type values.
 */
package org.urbancode.ucadf.core.model.ucd.version

import com.fasterxml.jackson.annotation.JsonValue

enum UcdVersionArtifactContainerTypeEnum {
	/** The type is a partined data set. */
	PDS("PDS"),
	
	/** The type is a partined data set. */
	DIRECTORY("directory")
	
	private String containerType
	
	// Constructor.	
	UcdVersionArtifactContainerTypeEnum(final String containerType) {
		this.containerType = containerType
	}

	@JsonValue	
	public String getContainerType() {
		return containerType
	}
}
