/**
 * This class instantiates component template objects.
 */
package org.urbancode.ucadf.core.model.ucd.componentTemplate

import org.urbancode.ucadf.core.model.ucd.component.UcdComponentTypeEnum
import org.urbancode.ucadf.core.model.ucd.general.UcdSecurityTypeObject
import org.urbancode.ucadf.core.model.ucd.property.UcdPropDef
import org.urbancode.ucadf.core.model.ucd.property.UcdPropSheet
import org.urbancode.ucadf.core.model.ucd.property.UcdProperty
import org.urbancode.ucadf.core.model.ucd.security.UcdExtendedSecurity
import org.urbancode.ucadf.core.model.ucd.security.UcdSecurityPermissionProperties
import org.urbancode.ucadf.core.model.ucd.tag.UcdTag
import org.urbancode.ucadf.core.model.ucd.user.UcdUser

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class UcdComponentTemplate extends UcdSecurityTypeObject {
	/** The component template ID. */
	String id
	
	/** The name. */
	String name
	
	/** The description. */
	String description

	/** The source configuration plugin name. */	
	String sourceConfigPluginName
	
	/** The number of versions. */
	Long versionCount
	
	/** The current version. */
	Long version
		
	/** The path. */
	String path
	
	/** The component type. */
	UcdComponentTypeEnum componentType

	/** The flag that indicates ignore qualifiers. */	
	Long ignoreQualifiers
	
	/** The created date. */
	Long created
	
	/** The flag that indicates the component template is active. */
	Boolean active
	
	/** The tags on the component template. */
	List<UcdTag> tags
	
	/** The user. */
	UcdUser user
	
	/** The commit information. */
	UcdComponentTemplateCommit commit

	/** The property definitions. */	
	List<UcdPropDef> propDefs
	
	/** The environment property definitions. */
	List<UcdPropDef> envPropDefs
	
	/** The resource property definitions. */
	List<UcdPropDef> resPropDefs

	/** The property values. */	
	List<UcdProperty> propValues

	/** The property sheet. */	
	UcdPropSheet propSheet
	
	/** The security resource ID. */
	String securityResourceId

	/** The security properties. */
	UcdSecurityPermissionProperties security

	/** The extended security. */
	UcdExtendedSecurity extendedSecurity
	
	// Constructors.	
	UcdComponentTemplate() {
	}
}
