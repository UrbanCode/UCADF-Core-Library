package org.urbancode.ucadf.core.model.ucd.snapshotConfiguration

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class UcdSnapshotConfigurationAdHocProperties extends UcdSnapshotConfigurationProperties {
	public final static String TYPE_NAME = "Ad-Hoc Properties"
	
	public show(Integer indent = 0) {
		super.show("Ad-Hoc Properties", indent)
	}
}
