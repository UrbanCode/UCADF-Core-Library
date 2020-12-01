package org.urbancode.ucadf.core.model.ucd.snapshotConfiguration

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonSubTypes.Type
import com.fasterxml.jackson.annotation.JsonTypeInfo.As

// Objects that are identifiable by the className property
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = As.EXISTING_PROPERTY, property = UcdSnapshotConfigurationEntity.NODENAME_CLASSNAME, visible = true)
@JsonSubTypes([
	@Type(value = UcdSnapshotConfigurationPropSheet.class, name = UcdSnapshotConfigurationPropSheet.CLASS_NAME),
	@Type(value = UcdSnapshotConfigurationApprovalProcess.class, name = UcdSnapshotConfigurationApprovalProcess.CLASS_NAME),
	@Type(value = UcdSnapshotConfigurationApplicationProcess.class, name = UcdSnapshotConfigurationApplicationProcess.CLASS_NAME),
	@Type(value = UcdSnapshotConfigurationComponentProcess.class, name = UcdSnapshotConfigurationComponentProcess.CLASS_NAME),
	@Type(value = UcdSnapshotConfigurationPromotedComponentProcess.class, name = UcdSnapshotConfigurationPromotedComponentProcess.CLASS_NAME)
])
interface UcdSnapshotConfigurationTypeByClassName {
}
