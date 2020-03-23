/**
 * This class instantiates a user object.
 */
package org.urbancode.ucadf.core.model.ucd.user

import org.urbancode.ucadf.core.model.ucadf.UcAdfSecureString
import org.urbancode.ucadf.core.model.ucd.general.UcdObject
import org.urbancode.ucadf.core.model.ucd.group.UcdGroup

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class UcdUser extends UcdObject {
	/** The user ID. */
	String id
	
	/** The name. */
	String name

	/** The actual name. */	
	String actualName
	
	/** The display name. */
	String displayName
	
	/** The email. */
	String email
	
	/** The password. */
	UcAdfSecureString password
	
	/** The authentication realm. */
	String authenticationRealm
	
	/** The flag that indicates the user is deleted. */
	Boolean deleted

	/** The flag that indicates the user is locked out. */	
	Boolean isLockedOut
	
	/** The flag that indicates the user is deletable. */
	Boolean isDeletable
	
	/** The deleted date. */
	Date deletedDate
	
	/** The list of associated groups. */
	List<UcdGroup> groups

	// Constructors.	
	UcdUser() {
	}
}
