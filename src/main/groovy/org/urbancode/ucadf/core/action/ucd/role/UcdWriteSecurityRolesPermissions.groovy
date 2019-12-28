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
import org.urbancode.ucadf.core.model.ucd.system.UcdSession

import com.fasterxml.jackson.databind.ObjectMapper

class UcdWriteSecurityRolesPermissions extends UcAdfAction {
	/** The file name. */
	String fileName

	/** (Optional) If specified then gets security entities with names that match this regular expression. */
	String match = ""
	
	/** The remove others option should be written to the actions file. Default is false. */
	Boolean removeOthers = false

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

		logInfo("Writing security actions to file [${file.getAbsolutePath()}].")

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
		logInfo("Adding ${UcdCreateRole.getSimpleName()} actions.")
		
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
		logInfo("Adding ${UcdCreateSecuritySubtype.getSimpleName()} actions.")
		
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
		logInfo("Adding ${UcdAddRolePermissions.getSimpleName()} actions.")
		
		for (role in ucdRolesMap.keySet()) {
			UcdRole ucdRole = actionsRunner.runAction([
				action: UcdGetRole.getSimpleName(),
				role: role
			])

			Map<String, Map> securityTypePermissionsMapById = actionsRunner.runAction([
				action: UcdGetSecurityTypePermissions.getSimpleName(),
				returnAs: UcdGetSecurityTypePermissions.ReturnAsEnum.MAPBYID
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
							permissions: ucdRolePermissionMap.getSubtypePermissions(type, subtype)
						]
					)
				
					// When exporting 6.1 we add actions from newer UC versions to the roles for backward compatibility.
					if (ucdSession.isUcdVersion(UcdSession.UCDVERSION_61)) {
						addSecuritySubtypesFor61(
							ucdRolePermissionMap,
							role,
							type,
							subtype
						)
					}
					
					// When exporting 6.2 we add actions from newer UC versions to the roles for backward compatibility.
					if (ucdSession.isUcdVersion(UcdSession.UCDVERSION_62)) {
						addSecuritySubtypesFor62(
							ucdRolePermissionMap,
							role,
							type,
							subtype
						)
					}
				}
			}
		}
	}
	
	// Add create role permission actions for UCD 6.1.
	private addSecuritySubtypesFor61(
		final UcdRolePermissionMap ucdRolePermissionMap,
		final String role,
		final String type,
		final String subtype) {
		
		// When exporting 6.1 we add actions from newer UC versions to the roles for backward compatibility.
		ucdRolePermissionMap.getSubtypePermissions(type, subtype).each {
			// "Create *" for certain types we need to add the "Create * From Templates" action
			String addPermissionName = "$it From Template"
			if (UcdSecurityPermissionProperties.ACTIONNAMESNOTIN61.contains(addPermissionName)) {
				addSecurityPermissions(
					[
						action: UcdAddRolePermissions.getSimpleName(),
						role: role,
						type: type,
						subtype: subtype,
						removeOthers: false,
						permissions: [ addPermissionName ]
					]
				)
			}
			
			// "View Resources" we need to add the "Execute on Resources" action for 6.2+ compatibility.
			if (UcdSecurityPermissionProperties.PERMISSION_VIEWRESOURCES.equals(it)) {
				addSecurityPermissions(
					[
						action: UcdAddRolePermissions.getSimpleName(),
						role: role,
						type: type,
						subtype: subtype,
						removeOthers: false,
						permissions: [ UcdSecurityPermissionProperties.PERMISSION_EXECUTEONRESOURCES ]
					]
				)
			}
			
			// "View Agents" we need to add the "Execute on Agents" action for 6.2+ compatibility.
			if (UcdSecurityPermissionProperties.PERMISSION_VIEWAGENTS.equals(it)) {
				addSecurityPermissions(
					[
						action: UcdAddRolePermissions.getSimpleName(),
						role: role,
						type: type,
						subtype: subtype,
						removeOthers: false,
						permissions: [ UcdSecurityPermissionProperties.PERMISSION_EXECUTEONAGENTS ]
					]
				)
			}
			
			// "Manage Snapshots" we need to add the "Delete Snapshots" action for 7.0+ compatibility.
			if (UcdSecurityPermissionProperties.PERMISSION_MANAGESNAPSHOTS.equals(it)) {
				addSecurityPermissions(
					[
						action: UcdAddRolePermissions.getSimpleName(),
						role: role,
						type: type,
						subtype: subtype,
						removeOthers: false,
						permissions: [ UcdSecurityPermissionProperties.PERMISSION_DELETESNAPSHOTS ]
					]
				)
			}
		}
	}	

	// Add create role permission actions for UCD 6.2.
	private addSecuritySubtypesFor62(
		final UcdRolePermissionMap ucdRolePermissionMap,
		final String role,
		final String type,
		final String subtype) {
		
        ucdRolePermissionMap.getSubtypePermissions(type, subtype).each {
			// "Manage Snapshots" we need to add the "Delete Snapshots" action for 7.0+ compatibility.
			if (UcdSecurityPermissionProperties.PERMISSION_MANAGESNAPSHOTS.equals(it)) {
				addSecurityPermissions(
					[
						action: UcdAddRolePermissions.getSimpleName(), 
						role: role, 
						type: type, 
						subtype: subtype, 
						removeOthers: false, 
						permissions: [ UcdSecurityPermissionProperties.PERMISSION_DELETESNAPSHOTS ]	
					]
				)
			}
        }
	}
	
	// Add security permissions to the list.
	private addSecurityPermissions(Map securityPermissionMap) {
		println "Adding $securityPermissionMap"
		
		securityPermissions.add(securityPermissionMap)
	}
}
