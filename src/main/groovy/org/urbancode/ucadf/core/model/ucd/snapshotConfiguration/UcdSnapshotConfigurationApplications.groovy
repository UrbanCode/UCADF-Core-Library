package org.urbancode.ucadf.core.model.ucd.snapshotConfiguration

import groovy.util.logging.Slf4j

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@Slf4j
@JsonIgnoreProperties(ignoreUnknown = true)
class UcdSnapshotConfigurationApplications extends UcdSnapshotConfigurationEntity implements UcdSnapshotConfigurationTypeByName {
	public final static String TYPE_NAME = "Applications"

	List<UcdSnapshotConfigurationApplication> children
	
	public show() {
		println "\n=== Applications ==="
		children.each { it.show() }
	}
	
	@JsonIgnore
	public Boolean isSoftLocked() {
		Boolean locked = (children.size() == 0 ? true : children.every { it.isSoftLocked() })
		log.debug "UcdSnapshotConfigurationApplications locked=$locked"
		return locked
	}
}
