/**
 * This class instantiates security permission property objects.
 */
package org.urbancode.ucadf.core.model.ucd.security

import org.urbancode.ucadf.core.model.ucd.general.UcdObject

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
class UcdSecurityPermissionProperties extends UcdObject {
    public final static String PERMISSION_CREATECOMPONENTSFROMTEMPLATE = "Create Components From Template"
    public final static String PERMISSION_CREATEENVIRONMENTSFROMTEMPLATE = "Create Environments From Template"
	public final static String PERMISSION_EXECUTEONRESOURCES = "Execute on Resources"
	public final static String PERMISSION_VIEWRESOURCES = "View Resources"
	public final static String PERMISSION_EXECUTEONAGENTS = "Execute on Agents"
	public final static String PERMISSION_VIEWAGENTS = "View Agents"
	public final static String PERMISSION_MANAGESNAPSHOTS = "Manage Snapshots"
	public final static String PERMISSION_DELETESNAPSHOTS = "Delete Snapshots"
	
	// System Configuration actions added in 7.x.
	public final static String PERMISSION_EDITNETWORKSETTINGS = "Edit Network Settings"
	public final static String PERMISSION_MANAGEAUDITLOG = "Manage Audit Log"
	public final static String PERMISSION_MANAGEBLUEPRINTDESIGNERINTEGRATIONS = "Manage Blueprint Designer Integrations"
	public final static String PERMISSION_MANAGEDIAGNOSTICS = "Manage Diagnostics"
	public final static String PERMISSION_MANAGELOGGINGSETTINGS = "Manage Logging Settings"
	public final static String PERMISSION_MANAGENOTIFICATIONSCHEMES = "Manage Notification Schemes"
	public final static String PERMISSION_MANAGESTATUSES = "Manage Statuses"
	public final static String PERMISSION_MANAGESYSTEMPROPERTIES = "Manage System Properties"
	public final static String PERMISSION_RELEASELOCKS = "Release Locks"
	public final static String PERMISSION_VIEWAUDITLOG = "View Audit Log"
	public final static String PERMISSION_VIEWLOCKS = "View Locks"
	public final static String PERMISSION_VIEWNETWORKSETTINGS = "View Network Settings"
	public final static String PERMISSION_VIEWOUTPUTLOG = "View Output Log"
	
    public final static List<String> ACTIONNAMESNOTIN61 = [
        PERMISSION_CREATECOMPONENTSFROMTEMPLATE,
        PERMISSION_CREATEENVIRONMENTSFROMTEMPLATE,
		PERMISSION_EXECUTEONRESOURCES,
		PERMISSION_EXECUTEONAGENTS,
		PERMISSION_DELETESNAPSHOTS
    ]
	
	public final static List<String> ACTIONNAMESNOTIN62 = [
		PERMISSION_DELETESNAPSHOTS
	]

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

	// Agents.
	@JsonProperty(value=UcdSecurityPermissionProperties.PERMISSION_VIEWAGENTS)
	Boolean viewAgents
	@JsonProperty("Add to Agent Pool")
	Boolean addToAgentPool
	@JsonProperty("Manage Impersonation")
	Boolean manageImpersonation
	@JsonProperty("Manage Version Imports")
	Boolean manageVersionImports
	
	// Agent Pools.
	@JsonProperty("Create Agent Pools")
	Boolean createAgentPools
	@JsonProperty("Manage Pool Members")
	Boolean managePoolMembers
	@JsonProperty("View Agent Pools")
	Boolean viewAgentPools

	// Applications.
	@JsonProperty("Create Applications")
	Boolean createApplications
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
    @JsonProperty(value=UcdSecurityPermissionProperties.PERMISSION_CREATECOMPONENTSFROMTEMPLATE)
    Boolean createComponentsFromTemplate
	@JsonProperty("View Components")
	Boolean viewComponents
	@JsonProperty("Edit Components")
	Boolean editComponents
	@JsonProperty("Manage Configuration Templates")
	Boolean manageConfigurationTemplates
	@JsonProperty("Manage Versions")
	Boolean manageVersions

	// Environments.
	@JsonProperty("Create Environments")
	Boolean createEnvironments
    @JsonProperty(value=UcdSecurityPermissionProperties.PERMISSION_CREATEENVIRONMENTSFROMTEMPLATE)
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
	@JsonProperty(value=UcdSecurityPermissionProperties.PERMISSION_VIEWRESOURCES)
	Boolean viewResources
	@JsonProperty(value=UcdSecurityPermissionProperties.PERMISSION_EXECUTEONRESOURCES)
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
