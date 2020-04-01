/**
 * This class instantiates security permission property objects.
 */
package org.urbancode.ucadf.core.model.ucd.security

import org.urbancode.ucadf.core.model.ucd.general.UcdObject

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
class UcdSecurityPermissionProperties extends UcdObject {
	// When an export is done from an older version we want to add any permissions to it that will exist in a newer version and that need to be set to true by default to maintain the same behavior.
	// We do this by looking at a permission that is being exported and adding additional new permissions that are implied from that old permission.
	private static Map<String, List<String>> PERMISSIONSTOADD = [
		"Manage Versions": [ "Manage Version Status" ]
	]

	// Return a list of new permissions to add to export if an old permission exists.
	public static List<String> permissionsToAdd(final String oldPermission) {
		List<String> newPermissions = []
		
		if (PERMISSIONSTOADD.containsKey(oldPermission)) {
			newPermissions = PERMISSIONSTOADD.get(oldPermission)
		}
		
		return newPermissions
	}	
	
	// Common.
	Boolean read
	
	@JsonProperty("Delete")
	Boolean delete
	
	@JsonProperty("Edit Basic Settings")
	Boolean editBasicSettings
	
	@JsonProperty("Manage Processes")
	Boolean manageProcesses
	
	@JsonProperty("Manage Properties")
	Boolean manageProperties
	
	@JsonProperty("Manage Teams")
	Boolean manageTeams
	
	@JsonProperty("Create Resources")
	Boolean createResources
	
	@JsonProperty("Approve Promotion")
	Boolean approvePromotion
	
	@JsonProperty("write")
	Boolean write
	
	@JsonProperty("Manage Process Lock")
	Boolean manageProcessLock

	// Agents.
	@JsonProperty("View Agents")
	Boolean viewAgents
	
	@JsonProperty("Add to Agent Pool")
	Boolean addToAgentPool
	
	@JsonProperty("Manage Impersonation")
	Boolean manageImpersonation
	
	@JsonProperty("Manage Version Imports")
	Boolean manageVersionImports
	
	@JsonProperty("Execute on Agents")
	Boolean executeOnAgents
	
	@JsonProperty("Install Remote Agents")
	Boolean installRemoteAgents
	
	@JsonProperty("Manage Licenses")
	Boolean manageLicenses
	
	@JsonProperty("Upgrade Agents")
	Boolean upgradeAgents

	// Agent Pools.
	@JsonProperty("Create Agent Pools")
	Boolean createAgentPools
	
	@JsonProperty("Manage Pool Members")
	Boolean managePoolMembers
	
	@JsonProperty("View Agent Pools")
	Boolean viewAgentPools

	// Agent relays.
	@JsonProperty("View Agent Relays")
	Boolean viewAgentRelays

	// Applications.
	@JsonProperty("Create Applications")
	Boolean createApplications
	
    @JsonProperty("Create Applications From Template")
    Boolean createApplicationsFromTemplate
	
	@JsonProperty("View Applications")
	Boolean viewApplications
	
	@JsonProperty("Manage Blueprints")
	Boolean manageBlueprints
	
	@JsonProperty("Manage Components")
	Boolean manageComponents
	
	@JsonProperty("Manage Environments")
	Boolean manageEnvironments
	
	@JsonProperty("Delete Snapshots")
	Boolean deleteSnapshots
	
	@JsonProperty("Manage Snapshots")
	Boolean manageSnapshots
	
	@JsonProperty("Run Component Processes")
	Boolean runComponentProcesses

	// Components.
	@JsonProperty("Create Components")
	Boolean createComponents
	
    @JsonProperty("Create Components From Template")
    Boolean createComponentsFromTemplate
	
	@JsonProperty("View Components")
	Boolean viewComponents
	
	@JsonProperty("Edit Components")
	Boolean editComponents
	
	@JsonProperty("Manage Configuration Templates")
	Boolean manageConfigurationTemplates
	
	@JsonProperty("Manage Versions")
	Boolean manageVersions
	
	@JsonProperty("Manage Version Status")
	Boolean manageVersionStatus

	// Component templates
	@JsonProperty("Create Component Templates")
	Boolean createComponentTemplates
	
	@JsonProperty("View Component Templates")
	Boolean viewComponentTemplates

	// Environments.
	@JsonProperty("Create Environments")
	Boolean createEnvironments

	@JsonProperty("Create Environments From Template")
    Boolean createEnvironmentsFromTemplate
	
	@JsonProperty("Execute on Environments")
	Boolean executeOnEnvironments
	
	@JsonProperty("Manage Approval Processes")
	Boolean manageApprovalProcesses
	
	@JsonProperty("Manage Base Resources")
	Boolean manageBaseResources
	
	@JsonProperty("View Environments")
	Boolean viewEnvironments

	// Resources.
	@JsonProperty("View Resources")
	Boolean viewResources
	
	@JsonProperty("Execute on Resources")
	Boolean executeOnResources
	
	@JsonProperty("Manage Children")
	Boolean manageChildren
	
	@JsonProperty("Map to Environments")
	Boolean mapToEnvironments

	// Generic Processes.
	@JsonProperty("execute")
	Boolean execute
	
	@JsonProperty("Create Processes")
	Boolean createProcesses
	
	@JsonProperty("Execute Processes")
	Boolean executeProcesses
	
	@JsonProperty("View Processes")
	Boolean viewProcesses

	// Constructors.
	UcdSecurityPermissionProperties() {
	}
}
