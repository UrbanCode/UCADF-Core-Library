/**
 * This class instantiates component process objects.
 */
package org.urbancode.ucadf.core.model.ucd.componentProcess

import org.urbancode.ucadf.core.model.ucd.component.UcdComponent
import org.urbancode.ucadf.core.model.ucd.general.UcdObject
import org.urbancode.ucadf.core.model.ucd.property.UcdPropDef
import org.urbancode.ucadf.core.model.ucd.property.UcdPropSheetDef

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
	
	/** Flag that indicates if the process type takes a version, e.g. Deployment. */
	Boolean takesVersion
	
	/** The status. TODO: Enumeration? */
	String status
	
	/** The type of inventory action performed by the process. */
	String inventoryActionType
	
	/** The type of configuration action performed by the process. */
	String configActionType
	
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
		
	/** The associated component. */
	UcdComponent component
	
	/** The flag to indicate if the process is locked. */
	Boolean locked
	
	/** The property sheet. */
	UcdPropSheetDef propSheetDef
	
	/** The property definitions. */
	List<UcdPropDef> propDefs
	
	/** The unfilled property definitions. */
	List<UcdPropDef> unfilledProperties
	
	/** The unfilled property definitions. */
	List<UcdPropDef> unfilledDraftProperties
	
	// Constructors.	
	UcdComponentProcess() {
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
