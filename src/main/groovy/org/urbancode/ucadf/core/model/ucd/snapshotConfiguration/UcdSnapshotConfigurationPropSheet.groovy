package org.urbancode.ucadf.core.model.ucd.snapshotConfiguration

import groovy.util.logging.Slf4j
import org.urbancode.ucadf.core.model.ucd.property.UcdPropSheetDef
import org.urbancode.ucadf.core.model.ucd.property.UcdProperty

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@Slf4j
@JsonIgnoreProperties(ignoreUnknown = true)
class UcdSnapshotConfigurationPropSheet extends UcdSnapshotConfigurationEntity implements UcdSnapshotConfigurationTypeByClassName {
	public final static String CLASS_NAME = "PropSheet"
	
	String className
	List<UcdProperty> properties
		
	/** The path. */
	String path
	
	/** The number of versions. */
	Long versionCount
	
	/** The current version. */
	Long version
	
	Map commit
	Boolean versioned
	Boolean locked
	UcdPropSheetDef propSheetDef
	
	public show(Integer indent = 0) {
		println "\t".multiply(indent) + "--> [$name] PropSheet"
		println "\t".multiply(indent + 1) + this.toJsonString()
		println "\t".multiply(indent + 1) + "--> Locked: " + isSoftLocked()
		for (property in properties) {
			println "\t".multiply(indent + 1) + "--> Property [$property.name] [$property.value]"
			println "\t".multiply(indent + 2) + property.toJsonString()
		}
	}
	
	@JsonIgnore
	public isSoftLocked() {
		log.debug "UcdSnapshotConfigurationPropSheet locked=$locked"
		return locked
	}
}
