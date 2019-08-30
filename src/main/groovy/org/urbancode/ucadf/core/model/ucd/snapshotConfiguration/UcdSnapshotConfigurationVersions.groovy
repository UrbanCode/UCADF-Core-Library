package org.urbancode.ucadf.core.model.ucd.snapshotConfiguration

import groovy.util.logging.Slf4j

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@Slf4j
@JsonIgnoreProperties(ignoreUnknown = true)
class UcdSnapshotConfigurationVersions extends UcdSnapshotConfigurationEntity {
	public final static String TYPE_NAME = "Versions"
	
	List<UcdSnapshotConfigurationVersion> children
	
	public show (Integer indent = 0) {
		println "\t--> Versions"
		children.each { it.show(indent) }
	}
	
	@JsonIgnore
	public Boolean isSoftLocked() {
		Boolean locked = (children.size() == 0 ? true : children.every { it.isSoftLocked() })
		log.debug "UcdSnapshotConfigurationVersions locked=$locked"
		return locked
	}
}
