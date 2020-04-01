/**
 * This class instantiates component process objects.
 */
package org.urbancode.ucadf.core.model.ucd.componentProcess

import org.urbancode.ucadf.core.model.ucd.general.UcdObject

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class UcdComponentProcess extends UcdObject {
	/** The component process ID. */
	String id
	
	/** The name. */
	String name
	
	/** The description. */
	String description

	/** The default working directory. */	
	String defaultWorkingDir
	
	/** TODO: What is this? */
	Boolean takesVersion
	
	/** The status. TODO: Enumeration? */
	String status
	
	/** The flag that indicates the component process is active. */
	Boolean active
	
	/** The number of versions. */
	Long versionCount
	
	/** The current version. */
	Long version
	
	/** The flag that indicates commit. */
	Boolean commit
	
	/** The path. */
	String path

	/** The flag that indicates deleted. */
	Boolean deleted
		
	// Constructors.	
	UcdComponentProcess() {
	}
}
