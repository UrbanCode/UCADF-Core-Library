/**
 * This class instantiates application template objects.
 */
package org.urbancode.ucadf.core.model.ucd.applicationTemplate

import org.urbancode.ucadf.core.model.ucd.environmentTemplate.UcdEnvironmentTemplate
import org.urbancode.ucadf.core.model.ucd.general.UcdSecurityTypeObject
import org.urbancode.ucadf.core.model.ucd.property.UcdPropDef
import org.urbancode.ucadf.core.model.ucd.property.UcdPropSheetDef
import org.urbancode.ucadf.core.model.ucd.security.UcdExtendedSecurity
import org.urbancode.ucadf.core.model.ucd.security.UcdSecurityPermissionProperties

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
class UcdApplicationTemplate extends UcdSecurityTypeObject {
	/** The application template ID. */
	String id
	
	/** The name. */
	String name
	
	/** The description. */
	String description

	/** The number of versions. */
	Long versionCount
	
	/** The current version. */
	Long version
		
	/** The path. */
	String path
	
	/** The created date. */
	Long created
	
	/** The flag that indicates the application template is deleted. */
	Boolean deleted
	
	/** The commit information. */
	UcdApplicationTemplateCommit commit

	/** The property definitions. */	
	List<UcdPropDef> propDefs
	
	/** The security resource ID. */
	String securityResourceId

	/** The security properties. */
	UcdSecurityPermissionProperties security

	/** The extended security. */
	UcdExtendedSecurity extendedSecurity

	/** The property sheet. */
	@JsonProperty("PropSheetDef")
	UcdPropSheetDef propSheetDef
	
	/** The flag that indicates to enforce complete snapshots. */
	Boolean enforceCompleteSnapshots

	/** The list of tag requirements. */	
	List<UcdApplicationTemplateTagRequirement> tagRequirements
	
	/** The list of environment templates. */	
	List<UcdEnvironmentTemplate> environmentTemplates
	
	/** The flag that indicates the application template has a process. */
	Boolean hasProcess

	/** The notification scheme ID. */
	String notificationSchemeId
		
	// Constructors.	
	UcdApplicationTemplate() {
	}
}
