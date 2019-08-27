/**
 * This class instantiates role mapping objects.
 */
package org.urbancode.ucadf.core.model.ucd.role

import org.urbancode.ucadf.core.model.ucd.general.UcdObject
import org.urbancode.ucadf.core.model.ucd.group.UcdGroup
import org.urbancode.ucadf.core.model.ucd.user.UcdUser

class UcdRoleMapping extends UcdObject {
	/** The user. */
	UcdUser user
	
	/** The role. */
	UcdRole role
	
	/** The group. */
	UcdGroup group
}
