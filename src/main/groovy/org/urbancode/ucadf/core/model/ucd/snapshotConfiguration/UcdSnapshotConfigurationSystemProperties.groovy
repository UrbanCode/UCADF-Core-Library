package org.urbancode.ucadf.core.model.ucd.snapshotConfiguration

import com.fasterxml.jackson.annotation.JsonIgnore

class UcdSnapshotConfigurationSystemProperties extends UcdSnapshotConfigurationProperties {
	public final static String TYPE_NAME = "System Properties"
	
	public show() {
		println "=== System ==="
		super.show("System Properties")
	}
	
	@JsonIgnore
	public Boolean isSoftLocked() {
		return super.isSoftLocked()
	}
}
