package org.urbancode.ucadf.core.model.ucd.snapshotConfiguration

import org.urbancode.ucadf.core.model.ucd.applicationProcess.UcdApplicationProcessInventoryManagementTypeEnum
import org.urbancode.ucadf.core.model.ucd.applicationProcess.UcdApplicationProcessOfflineAgentHandlingEnum

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class UcdSnapshotConfigurationApplicationProcess extends UcdSnapshotConfigurationProcess implements UcdSnapshotConfigurationTypeByClassName {
	public final static String CLASS_NAME = "ApplicationProcess"

	/** The inventory management type. */	
	UcdApplicationProcessInventoryManagementTypeEnum inventoryManagementType
	
	/** The offline agent handling. */
	UcdApplicationProcessOfflineAgentHandlingEnum offlineAgentHandling
	
	/** The flag that indicates deleted. */
	Boolean deleted
	
	/** TODO: What's this? */
	String metadataType
}
