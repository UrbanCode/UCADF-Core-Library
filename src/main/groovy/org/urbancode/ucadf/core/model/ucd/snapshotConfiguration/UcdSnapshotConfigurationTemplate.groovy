package org.urbancode.ucadf.core.model.ucd.snapshotConfiguration

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class UcdSnapshotConfigurationTemplate extends UcdSnapshotConfigurationEntity {
	String className
		
	/** The path. */
	String path
	
	/** The number of versions. */
	Long versionCount
	
	/** The current version. */
	Long version
	
	Map commit
	String componentId
	String data
	Boolean locked
	
	@JsonIgnore
	public isSoftLocked() {
		return locked
	}
	
	public show(Integer indent = 0) {
		println "\t".multiply(indent) + "--> [$name]"
		println "\t".multiply(indent + 1) + this.toJsonString()
		println "\t".multiply(indent + 1) + "--> Locked: " + isSoftLocked()
	}
}
