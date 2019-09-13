/**
 * This class instantiates source config plugin objects.
 */
package org.urbancode.ucadf.core.model.ucd.plugin

import org.urbancode.ucadf.core.model.ucd.general.UcdObject
import org.urbancode.ucadf.core.model.ucd.property.UcdPropSheet

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class UcdSourceConfigPlugin extends UcdObject {
	/** The ID. */
	String id
	
	/** The plugin ID. */
	String pluginId
	
	/** The name. */
	String name
	
	/** The description. */
	String description

	/** The version. */
	String version
		
	/** The version number. */
	Long versionNumber
	
	/** The ghosted date. */
	Long ghostedDate
	
	/** The component property sheet definition. */
	UcdPropSheet componentPropSheetDef

	/** The import property sheet definition. */
	UcdPropSheet importPropSheetDef
		
	// Constructors.
	UcdSourceConfigPlugin() {
	}
}
