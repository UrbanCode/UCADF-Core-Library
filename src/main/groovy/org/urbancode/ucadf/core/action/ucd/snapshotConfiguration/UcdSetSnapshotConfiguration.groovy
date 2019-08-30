/**
 * This action sets a snapshot's configuration.
 */
package org.urbancode.ucadf.core.action.ucd.snapshotConfiguration

import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.action.ucd.snapshot.UcdGetSnapshot
import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.general.UcdObject
import org.urbancode.ucadf.core.model.ucd.snapshot.UcdSnapshot

class UcdSetSnapshotConfiguration extends UcAdfAction {
	/** The type of set to do. */
	enum TypeEnum {
		/** Reset all configuration values to latest. */
		RESETALLTOLATEST,
		
		/** Set the latest entries to current. */
		SETLATESTENTRIESTOCURRENT
	}
	
	// Action properties.
	/** The application name or ID. */
	String application
	
	/** The snapshot name or ID. */
	String snapshot
	
	/** The type of set to do. */
	TypeEnum type
	
	/**
	 * Runs the action.	
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExist()

		if (type == TypeEnum.RESETALLTOLATEST) {
			resetAllToLatest()
		} else {
			setLatestEntriesToCurrent()
		}
	}
	
	// Reset the snapshot configuration to latest.
	private resetAllToLatest() {
		logInfo("Resetting application [$application] snapshot [$snapshot] configuration to latest.")

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

		WebTarget target = ucdSession.getUcdWebTarget().path("/rest/deploy/snapshot/{snapshotId}/configuration")
			.resolveTemplate("snapshotId", snapshotId)
		logDebug("target=$target")

		Response response = target.request(MediaType.APPLICATION_JSON).delete()
		if (response.getStatus() == 204) {
			logInfo("Application [$application] snapshot [$snapshot] configuration reset to latest.")
		} else {
			throw new UcdInvalidValueException(response)
		}
	}

	// Set the snapshot entries to current.
	private setLatestEntriesToCurrent() {
		logInfo("Setting application [$application] snapshot [$snapshot] configuration latest entries to current.")

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

		WebTarget target = ucdSession.getUcdWebTarget().path("/rest/deploy/snapshot/{snapshotId}/configuration/lockAllToCurrent")
			.resolveTemplate("snapshotId", snapshotId)
		logDebug("target=$target")

		Response response = target.request(MediaType.APPLICATION_JSON).put(Entity.json(""))
		if (response.getStatus() == 204) {
			logInfo("Application [$application] snapshot [$snapshot] configuration latest entries locked to current.")
		} else {
			throw new UcdInvalidValueException(response)
		}
	}
}
