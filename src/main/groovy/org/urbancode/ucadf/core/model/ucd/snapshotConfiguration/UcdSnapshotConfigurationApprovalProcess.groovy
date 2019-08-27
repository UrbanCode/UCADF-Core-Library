package org.urbancode.ucadf.core.model.ucd.snapshotConfiguration

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class UcdSnapshotConfigurationApprovalProcess extends UcdSnapshotConfigurationPropSheet implements UcdSnapshotConfigurationTypeByClassName {
	public final static String TYPE_NAME = "Approval Process"
	public final static String CLASS_NAME = "ApprovalProcess"
	
	String className
	String environmentId
	
	public show() {
		println "\t".multiply(3) + "--> Approval Process"
		println "\t".multiply(4) + this.toJsonString()
		super.show(4)
	}
}
