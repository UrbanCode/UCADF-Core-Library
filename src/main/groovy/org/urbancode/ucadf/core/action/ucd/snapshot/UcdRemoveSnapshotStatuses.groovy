/**
 * This action removes statuses from a snapshot.
 */
package org.urbancode.ucadf.core.action.ucd.snapshot

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.general.UcdObject
import org.urbancode.ucadf.core.model.ucd.snapshot.UcdSnapshot

class UcdRemoveSnapshotStatuses extends UcAdfAction {
	// Action properties.
	/** The application name or ID. */
	String application
	
	/** The snapshot name or ID. */
	String snapshot

	/** The list of status names or IDs. */	
	List<String> statuses
	
	/**
	 * Runs the action.	
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExist()
		
		for (status in statuses) {
			addSnapshotStatus(status)
		}
	}
	
	public addSnapshotStatus(final String status) {
		logInfo("Removing application [$application] snapshot [$snapshot] status [$status].")

		String snapshotId = snapshot
		if (!UcdObject.isUUID(application)) {
			UcdSnapshot ucdSnapshot = actionsRunner.runAction([
				action: UcdGetSnapshot.getSimpleName(),
				actionInfo: false,
				application: application,
				snapshot: snapshot,
				failIfNotFound: true
			])
			snapshotId = ucdSnapshot.getId()
		}

		WebTarget target = ucdSession.getUcdWebTarget().path("/rest/deploy/snapshot/{snapshotId}/status/{status}")
			.resolveTemplate("snapshotId", snapshotId)
			.resolveTemplate("status", status)
		logDebug("target=$target")
		
		Response response = target.request(MediaType.APPLICATION_JSON).delete()
		if (response.getStatus() == 204) {
			logInfo("Application [$application] snapshot [$snapshot] status [$status] removed.")
		} else {
			throw new UcdInvalidValueException(response)
		}
	}
}
