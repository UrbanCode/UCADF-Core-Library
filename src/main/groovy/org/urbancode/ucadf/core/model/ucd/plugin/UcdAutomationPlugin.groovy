/**
 * This class instantiates source config plugin objects.
 */
package org.urbancode.ucadf.core.model.ucd.plugin

import org.urbancode.ucadf.core.model.ucd.general.UcdObject

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class UcdAutomationPlugin extends UcdObject {
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
		
	// Constructors.
	UcdAutomationPlugin() {
	}
}
