package org.urbancode.ucadf.core.model.ucd.snapshotConfiguration

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

//@JsonIgnoreProperties(ignoreUnknown = true)
class UcdSnapshotConfigurationPromotedComponentProcess extends UcdSnapshotConfigurationProcess implements UcdSnapshotConfigurationTypeByClassName {
	public final static String CLASS_NAME = "PromotedComponentProcess"
}
