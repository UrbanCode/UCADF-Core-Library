/**
 * This class instantiates application process objects.
 */
package org.urbancode.ucadf.core.model.ucd.applicationProcess

import org.urbancode.ucadf.core.model.ucd.application.UcdApplication
import org.urbancode.ucadf.core.model.ucd.general.UcdObject
import org.urbancode.ucadf.core.model.ucd.property.UcdPropDef
import org.urbancode.ucadf.core.model.ucd.property.UcdPropSheetDef

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class UcdApplicationProcess extends UcdObject {
	/** The application process ID. */
	String id
	
	/** The name. */
	String name
	
	/** The description. */
	String description
	
	/** The inventory management type. */
	UcdApplicationProcessInventoryManagementTypeEnum inventoryManagementType
	
	/** The offline handling. */
	UcdApplicationProcessOfflineAgentHandlingEnum offlineAgentHandling
	
	/** The root activity. */
	Map rootActivity
	
	/** The flag that indicates if the process is active. */
	Boolean active
	
	/** The flag that indicates if the process is deleted. */
	Boolean deleted
	
	/** The number of versions. */
	Long versionCount
	
	/** The current version. */
	Long version
	
	/** The flag that indicates commit. */
	Boolean commit
		
	/** The path. */
	String path
	
	/** The associated application object. */
	UcdApplication application
	
	/** The required role ID. */
	String requiredRoleId
	
	/** The required role name. */
	String requiredRoleName
	
	/** The property sheet. */
	UcdPropSheetDef propSheetDef
	
	/** The property definitions. */
	List<UcdPropDef> propDefs
	
	/** TODO: What is this? */
	List componentsTakingVersions
	
	/** TODO: What is this? */
	List versionPresets
	
	/** TODO: What is this? */
	String metadataType
	
	// Constructors.
	UcdApplicationProcess() {
	}

	/**
	 * Get a property definition by name.
	 * @param name
	 * @return
	 */
	public UcdPropDef getPropDefByName(final String name) {
		// Find the existing property definition.
		UcdPropDef ucdPropDef = propDefs.find {
			it.getName().equals(name)
		}

		return ucdPropDef
	}
}
