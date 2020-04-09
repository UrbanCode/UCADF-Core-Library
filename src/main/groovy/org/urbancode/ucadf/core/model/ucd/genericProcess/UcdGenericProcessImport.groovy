/**
 * This class is used for a generic process import.
 */
package org.urbancode.ucadf.core.model.ucd.genericProcess

import org.urbancode.ucadf.core.model.ucd.importExport.UcdImport
import org.urbancode.ucadf.core.model.ucd.property.UcdPropDef
import org.urbancode.ucadf.core.model.ucd.property.UcdProperty
import org.urbancode.ucadf.core.model.ucd.security.UcdExtendedSecurityTeam

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class UcdGenericProcessImport extends UcdImport {
	/** The name. */
	String name
	
	/** The description. */
	String description
	
	/** The team mappings. */
	List<UcdExtendedSecurityTeam> teamMappings

	/** The root activity. TODO: Needs class. */
	Map rootActivity
	
	/** The property definitions. */
	List<UcdPropDef> propDefs
	
	/** The properties. */
	List<UcdProperty> properties
	
	/** The linked processes. TODO: Needs class. */
	List linkedProcesses
}
