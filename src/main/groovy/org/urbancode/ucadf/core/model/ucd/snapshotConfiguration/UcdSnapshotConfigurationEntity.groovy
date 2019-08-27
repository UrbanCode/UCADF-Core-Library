package org.urbancode.ucadf.core.model.ucd.snapshotConfiguration

import org.urbancode.ucadf.core.model.ucd.general.UcdObject

class UcdSnapshotConfigurationEntity extends UcdObject {
	public final static String NODENAME_ID = "id"
	public final static String NODENAME_NAME = "name"
	public final static String NODENAME_CHILDREN = "children"
	public final static String NODENAME_CLASSNAME = "className"
	
	/** The snapshot configuration ID. */
	String id
	
	/** The name. */
	String name

	public UcdSnapshotConfigurationEntity getChildByName(List<UcdSnapshotConfigurationEntity> children, String name) {
		return children.find { it.name == name }
	}
}
