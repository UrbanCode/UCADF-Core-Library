/**
 * This class is used to import an application process.
 */
package org.urbancode.ucadf.core.model.ucd.applicationProcess

import org.urbancode.ucadf.core.model.ucd.property.UcdPropDef
import org.urbancode.ucadf.core.model.ucd.system.UcdSession

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class UcdApplicationProcessImport {
	/** The application process name. */
	String name
	
	/** The description. */
	String description
	
	/** The root activity. */
	Map rootActivity
	
	/** The property definitions. */
	List<UcdPropDef> propDefs
	
	/** The inventory management type. */
	UcdApplicationProcessInventoryManagementTypeEnum inventoryManagementType
	
	/** The offline agent handling. */
	UcdApplicationProcessOfflineAgentHandlingEnum offlineAgentHandling
	
	/** The required role name. */
	String requiredRoleName
	
	/** The version presets. TODO: What is this? */
	List versionPresets
	
	/**
	 * Fixes the HTTP values in the property definitions.
	 * @param application The application name or ID.
	 * @param process The process name or ID.
	 */
	public fixHttpPropDefs(
		final String application, 
		final String process,
		final UcdSession ucdSession) {
		
		UcdPropDef.fixHttpPropDefs(
			application,
			process, 
			propDefs,
			ucdSession
		)
	}
}
