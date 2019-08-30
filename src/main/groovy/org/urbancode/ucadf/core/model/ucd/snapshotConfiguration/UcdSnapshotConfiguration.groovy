/**
 * This class is instantiates snapshot configuration objects.
 * The top-level configuration returned by the REST API is an array of sub-classes.
 */
package org.urbancode.ucadf.core.model.ucd.snapshotConfiguration

import org.urbancode.ucadf.core.model.ucd.general.UcdObject

import com.fasterxml.jackson.annotation.JsonIgnore

import groovy.util.logging.Slf4j

@Slf4j
class UcdSnapshotConfiguration extends UcdObject {
	@Delegate List<UcdSnapshotConfigurationTypeByName> children = []
	
	public UcdSnapshotConfigurationApplications getApplications() {
		return getChildByName(UcdSnapshotConfigurationApplications.TYPE_NAME)
	}
	
	public UcdSnapshotConfigurationComponents getComponents() {
		return getChildByName(UcdSnapshotConfigurationComponents.TYPE_NAME)
	}
	
	public UcdSnapshotConfigurationComponentTemplates getComponentTemplates() {
		return getChildByName(UcdSnapshotConfigurationComponentTemplates.TYPE_NAME)
	}

	public UcdSnapshotConfigurationResources getResources() {
		return getChildByName(UcdSnapshotConfigurationResources.TYPE_NAME)
	}
	
	public UcdSnapshotConfigurationSystemProperties getSystemProperties() {
		return getChildByName(UcdSnapshotConfigurationSystemProperties.TYPE_NAME)
	}

	public getChildByName(String name) {
		return children.find { (it as UcdSnapshotConfigurationEntity).name == name }
	}
	
	public show() {
		if (getApplications()) { getApplications().show() }
		if (getResources()) { getResources().show() }
		if (getComponents()) { getComponents().show() }
		if (getComponentTemplates()) { getComponentTemplates().show() }
		if (getSystemProperties()) { getSystemProperties().show() }
	}

	@JsonIgnore
	public isConfigurationSoftLocked() {
		Boolean applicationsLocked = getApplications() ? getApplications().isSoftLocked() : true
		Boolean resourcesLocked = getResources() ? getResources().isSoftLocked() : true
		Boolean componentsLocked = getComponents() ? getComponents().isSoftLocked() : true
		Boolean componentTemplatesLocked = getComponentTemplates() ? getComponentTemplates().isSoftLocked() : true
		Boolean systemPropertiesLocked = getSystemProperties() ? getSystemProperties().isSoftLocked() : true
		Boolean locked = (applicationsLocked && resourcesLocked && componentsLocked && componentTemplatesLocked && systemPropertiesLocked)
		log.debug "UcdSnapshotConfiguration locked=$locked"
		return locked
	}
}
