/**
 * This class instantiates component objects.
 */
package org.urbancode.ucadf.core.model.ucd.component

import org.urbancode.ucadf.core.model.ucd.agent.UcdAgent
import org.urbancode.ucadf.core.model.ucd.application.UcdApplication
import org.urbancode.ucadf.core.model.ucd.componentTemplate.UcdComponentTemplate
import org.urbancode.ucadf.core.model.ucd.general.UcdSecurityTypeObject
import org.urbancode.ucadf.core.model.ucd.plugin.UcdSourceConfigPlugin
import org.urbancode.ucadf.core.model.ucd.property.UcdPropSheet
import org.urbancode.ucadf.core.model.ucd.property.UcdPropSheetDef
import org.urbancode.ucadf.core.model.ucd.property.UcdProperty
import org.urbancode.ucadf.core.model.ucd.security.UcdExtendedSecurity
import org.urbancode.ucadf.core.model.ucd.security.UcdSecurityPermissionProperties
import org.urbancode.ucadf.core.model.ucd.tag.UcdTag
import org.urbancode.ucadf.core.model.ucd.version.UcdVersionTypeEnum

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class UcdComponent extends UcdSecurityTypeObject {
	// Common process properties.
	public final static String PROPNAME_ID = "component.id"
	public final static String PROPNAME_NAME = "component.name"
	
	/** The component ID. */
	String id
	
	/** The name. */
	String name
	
	/** The description. */
	String description

	/** The component type. */	
	UcdComponentTypeEnum componentType
	
	/** The flag that indicates ignore qualifiers. */
	Long ignoreQualifiers
	
	/** The flag that indicates import automatically. */
	Boolean importAutomatically
	
	/** The created date. */
	Long created
	
	/** The flag that indicates use VFS> */
	Boolean useVfs
	
	/** The flag that indicates this is an active component. */
	Boolean active

	/** The source configuration plugin name. */
	UcdSourceConfigPlugin sourceConfigPlugin
		
	/** The flag that indicates integration failed. */
	Boolean integrationFailed
	
	/** The flag that indicates deleted. */
	Boolean deleted
	
	/** The defaul version type. */
	UcdVersionTypeEnum defaultVersionType
	
	/** The number of cleanup days to keep. */
	Long cleanupDaysToKeep
	
	/** The cleanup count to keep. */
	Long cleanupCountToKeep
	
	/** The name of the user that created the component. */
	String user
	
	/** The template version. */
	Long templateVersion
	
	/** The tags on the component. */
	List<UcdTag> tags
	
	/** The component template tags. */
	List<UcdTag> templateTags
	
	/** The properties. */
	List<UcdProperty> properties
	
	/** The associated applications. */
	List<UcdApplication> applications
	
	/** The property sheet. */
	UcdPropSheet propSheet

	/** The environment property sheet definition. */
	UcdPropSheetDef environmentPropSheetDef
	
	/** The version property sheet definition. */
	UcdPropSheetDef versionPropSheetDef

	/** The template property sheet definition. */	
	UcdPropSheet templatePropSheet
	
	// TODO: Need class for this.
	Map resourceRole

	/** The system cleanup days to keep. */	
	Long systemCleanupDaysToKeep
	
	/** The system cleanup count to keep. */
	Long systemCleanupCountToKeep
	
	// TODO: Need type for this list.
	List templateSourceProperties
	
	/** The version creation process ID. */
	String versionCreationProcessId
	
	/** The version creation environment ID. */
	String versionCreationEnvironmentId
	
	/** The flag that indicates TODO: What is this? */
	Boolean hasTemplateRead
	
	/** TODO: What is this? */
	Boolean sourceConfigPluginNameMatchesPlugin
	
	/** The component template. */
	UcdComponentTemplate template
	
	/** The security resource ID. */
	String securityResourceId
	
	/** The security properties. */
	UcdSecurityPermissionProperties security
	
	/** The extended security. */
	UcdExtendedSecurity extendedSecurity

	/** The integration agent. */
	UcdAgent integrationAgent
	
	/** The integration tag. */
	UcdTag integrationTag
	
	// Constructors.	
	UcdComponent() {
	}

	@JsonIgnore	
	public Boolean hasTag(String tagName) {
		return tags.find { it.getName() == tagName }
	}
}
