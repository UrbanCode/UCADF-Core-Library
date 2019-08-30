package org.urbancode.ucadf.core.model.ucd.snapshotConfiguration

import groovy.util.logging.Slf4j
import org.urbancode.ucadf.core.model.ucd.property.UcdProperty

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@Slf4j
@JsonIgnoreProperties(ignoreUnknown = true)
class UcdSnapshotConfigurationProperties extends UcdSnapshotConfigurationEntity implements UcdSnapshotConfigurationTypeByName {
	List<UcdProperty> properties
		
	/** The path. */
	String path
	
	/** The number of versions. */
	Long versionCount
	
	/** The current version. */
	Long version
	
	Map commit
	Boolean versioned
	String className
	Boolean locked
	
	public show(String heading, Integer indent = 0) {
		println "\t".multiply(indent) + "--> $heading"
		println "\t".multiply(indent + 1) + this.toJsonString()
		println "\t".multiply(indent + 1) + "--> Locked: " + isSoftLocked()
		for (property in properties) {
			println "\t".multiply(indent + 1) + "--> Property [$property.name] [$property.value]"
			println "\t".multiply(indent + 2) + property.toJsonString()
		}
	}
	
	@JsonIgnore
	public isSoftLocked() {
		log.debug "UcdSnapshotConfigurationProperties locked=$locked"
		return locked
	}
}
