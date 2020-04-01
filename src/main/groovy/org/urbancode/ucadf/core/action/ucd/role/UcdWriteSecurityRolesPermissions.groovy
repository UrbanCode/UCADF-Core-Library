/**
 * This action writes the security actions to a file.
 */
package org.urbancode.ucadf.core.action.ucd.role

import org.urbancode.ucadf.core.action.ucd.security.UcdCreateSecuritySubtype
import org.urbancode.ucadf.core.action.ucd.security.UcdGetSecuritySubtypes
import org.urbancode.ucadf.core.action.ucd.security.UcdGetSecurityTypePermissions
import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.role.UcdRole
import org.urbancode.ucadf.core.model.ucd.role.UcdRolePermissionMap
import org.urbancode.ucadf.core.model.ucd.role.UcdRolesMap
import org.urbancode.ucadf.core.model.ucd.security.UcdSecurityPermissionProperties
import org.urbancode.ucadf.core.model.ucd.security.UcdSecuritySubtype
import org.urbancode.ucadf.core.model.ucd.security.UcdSecuritySubtypeMap

import com.fasterxml.jackson.databind.ObjectMapper

class UcdWriteSecurityRolesPermissions extends UcAdfAction {
	/** The file name. */
	String fileName

	/** (Optional) If specified then gets security entities with names that match this regular expression. */
	String match = ""
	
	/** The remove others option should be written to the actions file. Default is false. */
	Boolean removeOthers = false

	/** The failIfNotFound value to be written to the actions file. Default is true. */
	Boolean failIfNotFound = true
	
	// Private properties.
	private List<Map> securityPermissions = []
	private UcdRolesMap ucdRolesMap = [:]
	private List<UcdRole> ucdRoles = []
	
	// Keep a list of security subtypes encountered.
	private Set<String> securitySubtypes = new HashSet()

	/**
	 * Runs the action.	
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExist()

		File file = new File(fileName)
				
		// Initialize the temporary working directory.
		file.getParentFile()?.mkdirs()

		logVerbose("Writing security actions to file [${file.getAbsolutePath()}].")

		// Get the list of roles.
		ucdRoles = actionsRunner.runAction([
			action: UcdGetRoles.getSimpleName()
		])

		// Convert the roles list to a name map.
		ucdRolesMap = UcdRolesMap.getRolesNameMap(
			ucdRoles,
			match
		)

		// Add the create role actions.
		addCreateRoleActions()

		// Add the create security subtype actions.
		addCreateSecuritySubtypeActions()
		
		// Add the create role permission actions.
		addRolePermissions()

		// Write the JSON actions file as JSON.
		ObjectMapper mapper = new ObjectMapper()
		file.write(
			mapper.writer().withDefaultPrettyPrinter().writeValueAsString(
				[ actions: securityPermissions ]
			)
		)
	}

	// Add create role actions.
	private addCreateRoleActions() {
		logVerbose("Adding ${UcdCreateRole.getSimpleName()} actions.")
		
		// For each role to be added to security, add a create role action to the list.
		for (role in ucdRolesMap.keySet()) {
			addSecurityPermissions(
				[
					action: UcdCreateRole.getSimpleName(),
					name: ucdRolesMap.get(role).getName(),
					description: ucdRolesMap.get(role).getDescription() ?: "",
					failIfExists: false
				]
			)
		}
	}	

	// Add create security subtype actions.
	private addCreateSecuritySubtypeActions() {
		logVerbose("Adding ${UcdCreateSecuritySubtype.getSimpleName()} actions.")
		
		// Get the list of the security subtypes.
		List<UcdSecuritySubtype> ucdSecuritySubtypes = actionsRunner.runAction([
			action: UcdGetSecuritySubtypes.getSimpleName()
		])

		// Get a map of security subtypes that have a matching name.
		UcdSecuritySubtypeMap ucdSecuritySubtypeMap = new UcdSecuritySubtypeMap()

		for (ucdSecuritySubtype in ucdSecuritySubtypes) {
			if (!match || (ucdSecuritySubtype[UcdSecuritySubtypeMap.KEY_NAME] ==~ match)) {
				ucdSecuritySubtypeMap.put(
					ucdSecuritySubtype[UcdSecuritySubtypeMap.KEY_NAME],
					ucdSecuritySubtype
				)
			}
		}

		// For each subtype that matched.
		for (securitySubtype in ucdSecuritySubtypeMap.keySet()) {
			// Add to the list of subtypes encountered.
			securitySubtypes.add(securitySubtype)
			
			// For each subtype to be added to security, add a create security subtype action to the list.
			UcdSecuritySubtype ucdSecuritySubtype = ucdSecuritySubtypeMap[securitySubtype]
			addSecurityPermissions(
				[
					action: UcdCreateSecuritySubtype.getSimpleName(),
					name: ucdSecuritySubtype.getName(),
					description: ucdSecuritySubtype.getDescription() ?: "",
					type: ucdSecuritySubtype.getResourceType().getName(),
					failIfExists: false
				]
			)
		}
	}

	// Add role permission actions.
	private addRolePermissions() {
		logVerbose("Adding ${UcdAddRolePermissions.getSimpleName()} actions.")
		
		for (role in ucdRolesMap.keySet()) {
			UcdRole ucdRole = actionsRunner.runAction([
				action: UcdGetRole.getSimpleName(),
				role: role
			])

			Map<String, Map> securityTypePermissionsMapById = actionsRunner.runAction([
				action: UcdGetSecurityTypePermissions.getSimpleName(),
				returnAs: UcdGetSecurityTypePermissions.ReturnAsEnum.MAPBYID
			])

			Map<String, Map> securityTypePermissionsMapByName = actionsRunner.runAction([
				action: UcdGetSecurityTypePermissions.getSimpleName(),
				returnAs: UcdGetSecurityTypePermissions.ReturnAsEnum.MAPBYNAME
			])

			UcdRolePermissionMap ucdRolePermissionMap = ucdRole.getRolePermissionMap(securityTypePermissionsMapById)
			
			for (type in ucdRolePermissionMap.getTypes()) {
				for (subtype in ucdRolePermissionMap.getSubtypes(type)) {
					// Make sure the security subtype is being imported for the action.
					if (subtype && !securitySubtypes.contains(subtype)) {
						continue
					}
					
					addSecurityPermissions(
						[
							action: UcdAddRolePermissions.getSimpleName(),
							role: role,
							type: type,
							subtype: subtype,
							removeOthers: removeOthers,
							failIfNotFound: failIfNotFound,
							permissions: ucdRolePermissionMap.getSubtypePermissions(type, subtype)
						]
					)
					
					// When an export is done from an older version we want to add any permissions to it that don't exist in that version
					// but may exist in a newer version and that needs to be set to true by default to maintain the same behavior.
					for (permission in ucdRolePermissionMap.getSubtypePermissions(type, subtype)) {
						for (newPermission in UcdSecurityPermissionProperties.permissionsToAdd(permission)) {
							// Only add the new permission if it's not a permission that's known to the version being exported.
							println "Adding a permission that doesn't exist in the UCD version being exported but may existing in a new UCD version."
							if (!securityTypePermissionsMapByName.get(type)?.get(newPermission)) {
								addSecurityPermissions(
									[
										action: UcdAddRolePermissions.getSimpleName(),
										role: role,
										type: type,
										subtype: subtype,
										removeOthers: removeOthers,
										failIfNotFound: failIfNotFound,
										permissions: [ newPermission ]
									]
								)
							}
						}
					}
				}
			}
		}
	}
	
	// Add security permissions to the list.
	private addSecurityPermissions(Map securityPermissionMap) {
		println "Adding $securityPermissionMap"
		
		securityPermissions.add(securityPermissionMap)
	}
}
