/**
 * This class instantiates role permission objects.
 */
package org.urbancode.ucadf.core.model.ucd.role

import org.urbancode.ucadf.core.model.ucd.general.UcdObject
import org.urbancode.ucadf.core.model.ucd.security.UcdSecurityTypeEnum

class UcdRolePermission extends UcdObject {
	/** The role. */
	String role
	
	/** The security type. */
	UcdSecurityTypeEnum type
	
	/** The subtype. */
	String subtype
	
	/** The permission name. */
	String permissionName
	
	/** The permission ID. */
	String permissionId

	// Constructors.	
	UcdRolePermission() {
	}
}
