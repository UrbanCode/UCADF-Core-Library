package org.urbancode.ucadf.core.model.ucd.snapshotConfiguration

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonSubTypes.Type
import com.fasterxml.jackson.annotation.JsonTypeInfo.As

// Objects that are identifiable by the name property
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = As.EXISTING_PROPERTY, property = UcdSnapshotConfigurationEntity.NODENAME_NAME, visible = true)
@JsonSubTypes([
	// Top-level sub-types
	@Type(value = UcdSnapshotConfigurationApplications.class, name = UcdSnapshotConfigurationApplications.TYPE_NAME),
	@Type(value = UcdSnapshotConfigurationEnvironments.class, name = UcdSnapshotConfigurationEnvironments.TYPE_NAME),
	@Type(value = UcdSnapshotConfigurationComponents.class, name = UcdSnapshotConfigurationComponents.TYPE_NAME),
	@Type(value = UcdSnapshotConfigurationComponentTemplates.class, name = UcdSnapshotConfigurationComponentTemplates.TYPE_NAME),
	@Type(value = UcdSnapshotConfigurationSystemProperties.class, name = UcdSnapshotConfigurationSystemProperties.TYPE_NAME),
	@Type(value = UcdSnapshotConfigurationResources.class, name = UcdSnapshotConfigurationResources.TYPE_NAME),
	@Type(value = UcdSnapshotConfigurationComponentProperties.class, name = UcdSnapshotConfigurationComponentProperties.TYPE_NAME),
	@Type(value = UcdSnapshotConfigurationAdHocProperties.class, name = UcdSnapshotConfigurationAdHocProperties.TYPE_NAME),
	@Type(value = UcdSnapshotConfigurationTemplateProperties.class, name = UcdSnapshotConfigurationTemplateProperties.TYPE_NAME)
])
interface UcdSnapshotConfigurationTypeByName {
}
