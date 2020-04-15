/**
 * This class instantiates resource template objects.
 */
package org.urbancode.ucadf.core.model.ucd.resourceTemplate

import org.urbancode.ucadf.core.model.ucd.general.UcdSecurityTypeObject
import org.urbancode.ucadf.core.model.ucd.property.UcdPropDef
import org.urbancode.ucadf.core.model.ucd.property.UcdPropSheet
import org.urbancode.ucadf.core.model.ucd.property.UcdPropSheetDef
import org.urbancode.ucadf.core.model.ucd.security.UcdExtendedSecurity
import org.urbancode.ucadf.core.model.ucd.security.UcdSecurityPermissionProperties

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class UcdResourceTemplate extends UcdSecurityTypeObject {
	/** The resource ID. */
	String id
	
	/** The name. */
	String name
	
	/** The description. */
	String description
	
	/** The security resource ID. */
	String securityResourceId
	
	/** The flag to indicate the resource template is deleted. */
	Boolean deleted
	
	/** The property sheet. */
	UcdPropSheetDef propSheetDef
	
	/** The property sheet. */	
	UcdPropSheet propSheet

	/** The extended security. */
	UcdExtendedSecurity extendedSecurity

	/** The security properties. */
	UcdSecurityPermissionProperties security

	/** The property definitions. */	
	List<UcdPropDef> propDefs
	
	// Constructors.
	UcdResourceTemplate() {
	}
}
