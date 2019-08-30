package org.urbancode.ucadf.core.model.ucd.snapshotConfiguration

import org.urbancode.ucadf.core.model.ucd.property.UcdPropSheetDef

import com.fasterxml.jackson.annotation.JsonIgnore

class UcdSnapshotConfigurationTemplateProperties extends UcdSnapshotConfigurationProperties {
	public final static String TYPE_NAME = "template"
	
	UcdPropSheetDef propSheetDef
	
	@JsonIgnore
	public Boolean isSoftLocked() {
		return super.isSoftLocked()
	}
}
