/**
 * This class instantiates a generic process object.
 */
package org.urbancode.ucadf.core.model.ucd.genericProcess

import org.urbancode.ucadf.core.model.ucd.general.UcdSecurityTypeObject
import org.urbancode.ucadf.core.model.ucd.property.UcdPropDef
import org.urbancode.ucadf.core.model.ucd.property.UcdPropSheetDef
import org.urbancode.ucadf.core.model.ucd.property.UcdProperty
import org.urbancode.ucadf.core.model.ucd.security.UcdExtendedSecurity
import org.urbancode.ucadf.core.model.ucd.security.UcdSecurityPermissionProperties

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class UcdGenericProcess extends UcdSecurityTypeObject {
	/** The default working directory. */
	public final static String WORKINGDIRECTORY_DEFAULT = '${p:resource/work.dir}/${p:process.name}'
	
	/** The generic process ID. */
	String id
	
	/** The name. */
	String name
	
	/** The description. */
	String description

	/** The path. */	
	String path
	
	/** The number of versions. */
	Long versionCount
	
	/** The current version. */
	Long version
	
	/** The commit. */
	Long commit
	
	/** The default resource. (Deprecated at 7.0.4) */
	String defaultResourceId

	/** The default resources. (Added 7.0.4) */
	List<String> defaultResourceIds
		
	/** The notification scheme. */
	String notificationSchemeId
	
	/** The properties. */
	List<UcdProperty> properties

	/** The root actiity. */	
	Map rootActivity
	
	/** The property sheet definition. */
	UcdPropSheetDef propSheetDef
	
	/** The property definitions. */
	List<UcdPropDef> propDefs
	
	/** The unfilled properties. TODO: Needs class. */
	List<Object> unfilledProperties
	
	/** The security resource ID. */
	String securityResourceId

	/** The security properties. */
	UcdSecurityPermissionProperties security
	
	/** The extended security. */
	UcdExtendedSecurity extendedSecurity
	
	// Constructors.	
	UcdGenericProcess() {
	}
	
	/**
	 * Get the working directory. (This is contained in the property values.)
	 * @return The working directory.
	 */
	@JsonIgnore
	public String getWorkingDir() {
		String workingDir
		
		UcdProperty ucdProperty = properties.find {
			it.getName() == "workingDir"
		}
		
		if (ucdProperty) {
			workingDir = ucdProperty.getValue()
		}
		
		return workingDir
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
