/**
 * This class instantiates application objects.
 */
package org.urbancode.ucadf.core.model.ucd.application

import org.urbancode.ucadf.core.model.ucd.general.UcdSecurityTypeObject
import org.urbancode.ucadf.core.model.ucd.property.UcdPropSheet
import org.urbancode.ucadf.core.model.ucd.property.UcdProperty
import org.urbancode.ucadf.core.model.ucd.security.UcdExtendedSecurity
import org.urbancode.ucadf.core.model.ucd.security.UcdSecurityPermissionProperties
import org.urbancode.ucadf.core.model.ucd.tag.UcdTag

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class UcdApplication extends UcdSecurityTypeObject {
	/** The application ID. */
	String id
	
	/** The name. */
	String name
	
	/** The description. */
	String description
	
	Long created
	Boolean active
	Boolean deleted
	Boolean enforceCompleteSnapshots
	List<UcdTag> tags
	String user
	Long componentCount

	/** The property sheet. */	
	UcdPropSheet propSheet
	
	Boolean hasZosComponent
	UcdPropSheet templatePropSheet
	List<UcdProperty> properties
	
	/** The security resource ID. */
	String securityResourceId

	/** The security properties. */
	UcdSecurityPermissionProperties security
	
	/** The extended security. */
	UcdExtendedSecurity extendedSecurity

	Long templateVersion
	String templateId

	String notificationSchemeId	
	
	// Constructors.
	UcdApplication() {
	}
}
