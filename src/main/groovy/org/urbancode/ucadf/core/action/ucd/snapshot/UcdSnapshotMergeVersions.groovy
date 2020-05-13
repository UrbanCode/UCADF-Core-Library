/**
 * This action adds statuses to a snapshot.
 */
package org.urbancode.ucadf.core.action.ucd.snapshot

import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.action.ucd.component.UcdGetComponent
import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucd.component.UcdComponent
import org.urbancode.ucadf.core.model.ucd.general.UcdObject
import org.urbancode.ucadf.core.model.ucd.snapshot.UcdSnapshot

import groovy.json.JsonBuilder

class UcdSnapshotMergeVersions extends UcAdfAction {
	// Action properties.
	/** The application name or ID. */
	String application
	
	/** The snapshot name or ID. */
	String snapshot
	
	/** The component name to merge. */
	String component

	/** The version name to create. */
	String version
	
	/** The flag that indicates fail if the version already exists. Default is true. */
	Boolean failIfExists = true

	/**
	 * Runs the action.	
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExist()
		
		logVerbose("Merging application [$application] snapshot [$snapshot] component [$component] version [$version].")

		String snapshotId = snapshot
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

		String componentId = component
		if (!UcdObject.isUUID(component)) {
			UcdComponent ucdComponent = actionsRunner.runAction([
				action: UcdGetComponent.getSimpleName(),
				actionInfo: false,
				actionVerbose: false,
				component: component,
				failIfNotFound: true
			])
			componentId = ucdComponent.getId()
		}

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
