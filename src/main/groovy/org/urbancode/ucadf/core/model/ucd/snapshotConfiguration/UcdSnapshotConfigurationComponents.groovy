package org.urbancode.ucadf.core.model.ucd.snapshotConfiguration

import groovy.util.logging.Slf4j

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@Slf4j
@JsonIgnoreProperties(ignoreUnknown = true)
class UcdSnapshotConfigurationComponents extends UcdSnapshotConfigurationEntity implements UcdSnapshotConfigurationTypeByName {
	public final static String TYPE_NAME = "Components"

	List<UcdSnapshotConfigurationComponent> children
	
	public show() {
		println "=== Components ==="
		children.each { it.show() }
	}
	
	
	@JsonIgnore
	public Boolean isSoftLocked() {
		Boolean locked = (children.size() == 0 ? true : children.every { it.isSoftLocked() })
		log.debug "UcdSnapshotConfigurationComponents locked=$locked"
		return locked
	}
}
