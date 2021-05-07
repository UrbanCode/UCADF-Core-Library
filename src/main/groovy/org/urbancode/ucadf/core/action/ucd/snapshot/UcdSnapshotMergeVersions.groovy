/**
 * This action adds statuses to a snapshot.
 */
package org.urbancode.ucadf.core.action.ucd.snapshot

import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.action.ucd.component.UcdGetComponent
import org.urbancode.ucadf.core.action.ucd.version.UcdAddVersionFiles
import org.urbancode.ucadf.core.action.ucd.version.UcdAddVersionLink
import org.urbancode.ucadf.core.action.ucd.version.UcdCreateVersion
import org.urbancode.ucadf.core.action.ucd.version.UcdDeleteVersion
import org.urbancode.ucadf.core.action.ucd.version.UcdDownloadVersionFiles
import org.urbancode.ucadf.core.action.ucd.version.UcdGetVersionLinks
import org.urbancode.ucadf.core.action.ucd.version.UcdGetVersionProperties
import org.urbancode.ucadf.core.action.ucd.version.UcdSetVersionProperties
import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucd.component.UcdComponent
import org.urbancode.ucadf.core.model.ucd.component.UcdComponentTypeEnum
import org.urbancode.ucadf.core.model.ucd.general.UcdObject
import org.urbancode.ucadf.core.model.ucd.property.UcdProperty
import org.urbancode.ucadf.core.model.ucd.snapshot.UcdSnapshot
import org.urbancode.ucadf.core.model.ucd.version.UcdVersionLink

import groovy.json.JsonBuilder

class UcdSnapshotMergeVersions extends UcAdfAction {
	/** The phase of the merge that is currently running. */
	enum MergePhaseEnum {
		/** The version downloads are beginning. */
		DownloadVersionsBegin,
		
		/** A version has been downloaded and overlayed the previous version. */
		DownloadedVersion,

		/** The version have been downloaded and overlayed. */
		DownloadVersionsEnd
	}
	
	// Action properties.
	/** The application name or ID. */
	String application
	
	/** The snapshot name or ID. */
	String snapshot
	
	/** The component name to merge. */
	String component

	/** The version name to create. */
	String version

	/** The empty directory to which STANDARD component type artifacts will be downloaded for merging. Not required for a z/OS component. */
	File downloadDir

	/** The file name to use when downloading STANDARD component type artifacts. */
	String downloadFileName = "UCADF-Core-MergeArtifacts.zip"


	/** A flag that indicates whether to merge properties from all contributing versions. Default is false (use properties from last merged version.) */
	Boolean mergeAllProperties = false
	
	/** A flag that indicates whether to merge links from all contributing versions. Default is false (use links from last merged version.) */
	Boolean mergeAllLinks = false

	String callbackAction = ""
			
	/** The flag that indicates fail if the version already exists. Default is true. */
	Boolean failIfExists = true

	private String snapshotId
	private String componentId
	private Map<String, String> mergedPaths = [:]
	
	/**
	 * Runs the action.	
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExistExclude([ 'downloadDir'] )
		
		logVerbose("Merging application [$application] snapshot [$snapshot] component [$component] version [$version].")

		// Get the component information.
		UcdComponent ucdComponent = actionsRunner.runAction([
			action: UcdGetComponent.getSimpleName(),
			actionInfo: false,
			actionVerbose: false,
			component: component,
			failIfNotFound: true
		])
		componentId = ucdComponent.getId()

		// Get the snapshot information.
		snapshotId = snapshot
		if (!UcdObject.isUUID(application)) {
			UcdSnapshot ucdSnapshot = actionsRunner.runAction([
				action: UcdGetSnapshot.getSimpleName(),
				actionInfo: false,
				actionVerbose: false,
				application: application,
				snapshot: snapshot,
				failIfNotFound: true
			])
			snapshotId = ucdSnapshot.getId()
		}

		// Merge the versions.
		if (UcdComponentTypeEnum.ZOS.equals(ucdComponent.getComponentType())) {
			mergeZosComponent()
		} else {
			if (!downloadDir) {
				throw new UcAdfInvalidValueException("The downloadDir property must be provided for a STANDARD component merge.")
			}
			
			// Create the directory if it doesn't exist.
			downloadDir.mkdirs()
			
			if (downloadDir.list().size() > 0) {
				throw new UcAdfInvalidValueException("The downloadDir [${downloadDir.getPath()}] is not empty.")
			}

			mergeStandardComponent()
		}
	}

	// Merge a standard component type. There is no API to do this so each version has to be evaluated and downloaded to create a new version.
	public mergeStandardComponent() {
		// Get the snapshot versions map.
		Map<String, Map<String, String>> snapshotVersionsMap = actionsRunner.runAction([
			action: UcdGetSnapshotVersions.getSimpleName(),
			actionInfo: false,
			actionVerbose: false,
			application: application,
			snapshot: snapshotId,
			returnAs: UcdGetSnapshotVersions.ReturnAsEnum.MAPBYNAME
		])

		// Get the set of version names.
		HashSet versionSet = new HashSet()
		snapshotVersionsMap.get(component).each { versionName, value ->
			versionSet.add(versionName)
		}
		
		// Get a merged map of the version properties.
		Map<String, UcdProperty> mergedVersionPropertiesMap = [:]
		for (version in (mergeAllProperties ? versionSet : [ versionSet.last() ])) {
			logVerbose("Using properties from version [${version}].")
			Map<String, UcdProperty> versionPropertiesMap = actionsRunner.runAction([
				action: UcdGetVersionProperties.getSimpleName(),
				actionInfo: false,
				actionVerbose: false,
				component: component,
				version: version,
				returnAs: UcdGetVersionProperties.ReturnAsEnum.MAPBYNAME
			])
			mergedVersionPropertiesMap += versionPropertiesMap
		}
		
		// Create a list of the merged properties.
		List<UcdProperty> mergedVersionProperties = []
		mergedVersionPropertiesMap.each { key, ucdProperty ->
			mergedVersionProperties.add(ucdProperty)
		}

		// Get a merged list of the version links.
		Map<String, UcdVersionLink> mergedVersionLinksMap = [:]
		for (version in (mergeAllLinks ? versionSet : [ versionSet.last() ])) {
			logVerbose("Using links from version [${version}].")
			Map<String, UcdVersionLink> versionLinksMap = actionsRunner.runAction([
				action: UcdGetVersionLinks.getSimpleName(),
				actionInfo: false,
				actionVerbose: false,
				component: component,
				version: version,
				returnAs: UcdGetVersionLinks.ReturnAsEnum.MAPBYNAME
			])
			mergedVersionLinksMap += versionLinksMap
		}
		
		// If a callback action was provided then run it.
		if (callbackAction) {
			actionsRunner.runAction([
				action: callbackAction,
				actionInfo: actionInfo,
				actionVerbose: actionVerbose,
				mergePhase: MergePhaseEnum.DownloadVersionsBegin,
				component: component,
				extractDirName: downloadDir.getPath()
			])
		}

		// Download the version artifacts for the versions to be merged in the order they were specified in the snapshot.
		for (versionName in versionSet) {
			String fileName = new File(downloadDir, downloadFileName).getPath()
			
			// Download the version files.
			actionsRunner.runAction([
				action: UcdDownloadVersionFiles.getSimpleName(),
				actionInfo: false,
				component: component,
				version: versionName,
				fileName: fileName,
				extractDirName: downloadDir.getPath()
			])
			
			// If a callback action was provided then run it to do any custom renaming, etc.
			if (callbackAction) {
				actionsRunner.runAction([
					action: callbackAction,
					actionInfo: actionInfo,
					actionVerbose: actionVerbose,
					mergePhase: MergePhaseEnum.DownloadedVersion,
					component: component,
					version: versionName,
					fileName: fileName,
					extractDirName: downloadDir.getPath()
				])
			}
		}
		
		// If a callback action was provided then run it.
		if (callbackAction) {
			actionsRunner.runAction([
				action: callbackAction,
				actionInfo: actionInfo,
				actionVerbose: actionVerbose,
				mergePhase: MergePhaseEnum.DownloadVersionsEnd,
				component: component,
				extractDirName: downloadDir.getPath()
			])
		}

		try {
			// Create the new version.
			actionsRunner.runAction([
				action: UcdCreateVersion.getSimpleName(),
				actionInfo: false,
		        component: component,
		        name: version,
		        failIfExists: false
			])
	
			// Add the version files.
			actionsRunner.runAction([
				action: UcdAddVersionFiles.getSimpleName(),
				actionInfo: false,
		        component: component,
		        version: version,
		        base: downloadDir.getPath(),
				include: [ "**/*" ]
			])
			
			// Set the version properties.
			actionsRunner.runAction([
				action: UcdSetVersionProperties.getSimpleName(),
				actionInfo: false,
		        component: component,
		        version: version,
				properties: mergedVersionProperties
			])
			
			// Add the version links.
			mergedVersionLinksMap.each { key, ucdVersionLink ->
				actionsRunner.runAction([
					action: UcdAddVersionLink.getSimpleName(),
					actionInfo: false,
			        component: component,
			        version: version,
					name: ucdVersionLink.getName(),
					value: ucdVersionLink.getValue()
				])
			}
		} catch(Exception e) {
			// Delete a partially created version.
			actionsRunner.runAction([
				action: UcdDeleteVersion.getSimpleName(),
				actionInfo: false,
		        component: component,
		        version: version
			])

			throw(e)
		}
	}

	// Merge a z/OS component type. There is an API to do this for this component type.
	public mergeZosComponent() {
		Map requestMap = [
			version: version
		]

		JsonBuilder jsonBuilder = new JsonBuilder(requestMap)
		logDebug("jsonBuilder=$jsonBuilder")

		WebTarget target = ucdSession.getUcdWebTarget().path("/rest/deploy/snapshot/{snapshotId}/createCompressedVersion/{componentId}/{version}")
			.resolveTemplate("snapshotId", snapshotId)
			.resolveTemplate("componentId", componentId)
			.resolveTemplate("version", version)
		logDebug("target=$target")
		
		Response response = target.request(MediaType.WILDCARD).accept(MediaType.APPLICATION_JSON).put(Entity.text(jsonBuilder.toString()))
		if (response.getStatus() == 200) {
			logVerbose("Application [$application] snapshot [$snapshot] component [$component] merged version [$version] created.")
		} else {
			String errMsg = UcAdfInvalidValueException.getResponseErrorMessage(response)
			logVerbose(errMsg)
			if (!(response.getStatus() == 400 && (errMsg ==~ /.*already exists.*/ && !failIfExists))) {
				throw new UcAdfInvalidValueException(errMsg)
			}
		}
	}
}
