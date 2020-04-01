/**
 * This class is used to import component processes.
 */
package org.urbancode.ucadf.core.model.ucd.componentProcess

import org.urbancode.ucadf.core.model.ucd.property.UcdPropDef

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class UcdComponentProcessImport {
	/** The name. */
	String name
	
	/** The description. */
	String description

	/** The root activity. TODO: Need to add class. */	
	Map rootActivity
	
	/** The property definitions. */
	List<UcdPropDef> propDefs
	
	/** The default working directory. */
	String defaultWorkingDir
	
	/** TODO: What is this? */
	Boolean takesVersion
	
	/** The status. TODO: Need enumeration? */
	String status
	
	/** The flag that indicates the component process is active. */
	Boolean active
	
	/** The path. */
	String path
	
	/** The inventory action type. TODO: What is this? Enumeration? */
	String inventoryActionType
	
	/** The configuration action type. TODO: What is this? Enumeration? */
	String configActionType

	/** The flag to indicate deleted. */
	Boolean deleted
		
	// Constructors.
	UcdComponentProcessImport() {
	}
}
