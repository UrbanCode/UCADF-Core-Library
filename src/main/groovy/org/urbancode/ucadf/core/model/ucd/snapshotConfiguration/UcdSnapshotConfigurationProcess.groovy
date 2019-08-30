package org.urbancode.ucadf.core.model.ucd.snapshotConfiguration

import groovy.util.logging.Slf4j

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@Slf4j
@JsonIgnoreProperties(ignoreUnknown = true)
class UcdSnapshotConfigurationProcess extends UcdSnapshotConfigurationEntity {
	String className
	String description
	Boolean active
		
	/** The path. */
	String path
	
	/** The number of versions. */
	Long versionCount
	
	/** The current version. */
	Long version
	
	Map commit
	Boolean locked

	public show(Integer indent = 0) {
		println "\t".multiply(indent) + "--> [$name]"
		println "\t".multiply(indent + 1) + this.toJsonString()
		println "\t".multiply(indent + 1) + "--> Locked: " + isSoftLocked()
	}
	
	@JsonIgnore
	public Boolean isSoftLocked() {
		log.debug "UcdSnapshotConfigurationProcess [$name] locked=$locked"
		return locked
	}
}
