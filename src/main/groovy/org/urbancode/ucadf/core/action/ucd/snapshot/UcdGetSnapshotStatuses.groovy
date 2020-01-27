/**
 * This action gets a snapshot's statuses.
 */
package org.urbancode.ucadf.core.action.ucd.snapshot

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.GenericType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.general.UcdObject
import org.urbancode.ucadf.core.model.ucd.snapshot.UcdSnapshot
import org.urbancode.ucadf.core.model.ucd.status.UcdStatus

class UcdGetSnapshotStatuses extends UcAdfAction {
	/** The type of collection to return. */
	enum ReturnAsEnum {
		/** Return as a list of UcdSnapshot objects. */
		OBJECTS,
		
		/** Return as a list of snapshot names. */
		NAMES
	}

	// Action properties.
	/** The application name or ID. */
	String application
	
	/** The snapshot name or ID. */
	String snapshot

	/** The type of collection to return. */
	ReturnAsEnum returnAs = ReturnAsEnum.OBJECTS

	/**
	 * Runs the action.	
	 * @return The specified collection type.
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExist()

		List<UcdStatus> ucdStatuses = []

		logVerbose("Getting application [$snapshot] snapshot [$snapshot] statuses.")

		// If a snapshot ID was provided then use it. Otherwise get the snapshot information to get the ID.
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

		WebTarget target = ucdSession.getUcdWebTarget().path("/rest/deploy/snapshot/{snapshotId}/status")
			.resolveTemplate("snapshotId", snapshotId)
		logDebug("target=$target")
		
		Response response = target.request().get()
		if (response.getStatus() == 200) {
			ucdStatuses = response.readEntity(new GenericType<List<UcdStatus>>(){})
		} else {
			throw new UcdInvalidValueException(response)
		}

		Object returnList
		
		if (ReturnAsEnum.OBJECTS.equals(returnAs)) {
			returnList = ucdStatuses
		} else {
			List<String> namesList = []
			ucdStatuses.each {
				namesList.add(it.getName())
			}
			returnList = namesList
		}

		return returnList
	}
}
