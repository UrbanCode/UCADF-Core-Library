package org.urbancode.ucadf.core.model.ucd.snapshotConfiguration

import groovy.util.logging.Slf4j

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@Slf4j
@JsonIgnoreProperties(ignoreUnknown = true)
class UcdSnapshotConfigurationTemplates extends UcdSnapshotConfigurationEntity {
	public final static String TYPE_NAME = "Configuration Templates"
	
	List<UcdSnapshotConfigurationTemplate> children
	
	public show(Integer indent = 0) {
		println "\t".multiply(indent) + "--> $TYPE_NAME"
		children.each { it.show(indent + 1) }
	}
	
	@JsonIgnore
	public Boolean isSoftLocked() {
		Boolean locked = (children.size() == 0 ? true : children.every { it.isSoftLocked() })
		log.debug "UcdSnapshotConfigurationTemplates locked=$locked"
		return locked
	}
}
