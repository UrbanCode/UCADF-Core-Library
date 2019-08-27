package org.urbancode.ucadf.core.model.ucd.snapshotConfiguration

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class UcdSnapshotConfigurationComponentProcess extends UcdSnapshotConfigurationProcess implements UcdSnapshotConfigurationTypeByClassName {
	public final static String CLASS_NAME = "ComponentProcess"
	
	String defaultWorkingDir
	Boolean takesVersion
	String status
}
