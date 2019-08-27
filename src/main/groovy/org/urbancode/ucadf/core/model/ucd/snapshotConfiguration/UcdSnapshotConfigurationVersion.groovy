package org.urbancode.ucadf.core.model.ucd.snapshotConfiguration

import groovy.util.logging.Slf4j

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@Slf4j
@JsonIgnoreProperties(ignoreUnknown = true)
class UcdSnapshotConfigurationVersion extends UcdSnapshotConfigurationEntity {
	List<UcdSnapshotConfigurationPropSheet> children
	
	public UcdSnapshotConfigurationPropSheet getPropSheet() {
		return getChildByName(children, UcdSnapshotConfigurationVersionProperties.TYPE_NAME)
	}
	
	public show(Integer indent = 0) {
		println "\t\t--> [$name] Version"
		println "\t\t\t" + this.toJsonString()
		for (child in children) {
			UcdSnapshotConfigurationPropSheet propSheet = getPropSheet()
			propSheet.show(3)
		}
	}
	
	@JsonIgnore
	public Boolean isSoftLocked() {
		Boolean locked = (children.size() == 0 ? true : children.every { it.isSoftLocked() })
		log.debug "UcdSnapshotConfigurationVersion locked=$locked"
		return locked
	}
}
