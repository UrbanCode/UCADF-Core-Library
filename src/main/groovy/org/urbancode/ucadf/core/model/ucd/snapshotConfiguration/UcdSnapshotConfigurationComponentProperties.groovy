package org.urbancode.ucadf.core.model.ucd.snapshotConfiguration

import groovy.util.logging.Slf4j

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@Slf4j
@JsonIgnoreProperties(ignoreUnknown = true)
class UcdSnapshotConfigurationComponentProperties extends UcdSnapshotConfigurationEntity {
	public final static String TYPE_NAME = "Component Properties"
	
	List<UcdSnapshotConfigurationPropSheet> children
	
	public show() {
		println "\t\t\t--> Component Properties"
		println "\t\t\t\t" + this.toJsonString()
		children.each { it.show(4) }
	}
	
	@JsonIgnore
	public Boolean isSoftLocked() {
		Boolean locked = (children.size() == 0 ? true : children.every { it.isSoftLocked() })
		log.debug "UcdSnapshotConfigurationComponentProperties locked=$locked"
		return locked
	}
}
