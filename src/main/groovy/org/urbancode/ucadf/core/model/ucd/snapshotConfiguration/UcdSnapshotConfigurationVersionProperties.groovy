package org.urbancode.ucadf.core.model.ucd.snapshotConfiguration

import org.urbancode.ucadf.core.model.ucd.general.UcdObject

class UcdSnapshotConfigurationVersionProperties extends UcdSnapshotConfigurationEntity {
	public final static String TYPE_NAME = "Properties for Component"
	
	List<UcdSnapshotConfigurationPropSheet> children
}
