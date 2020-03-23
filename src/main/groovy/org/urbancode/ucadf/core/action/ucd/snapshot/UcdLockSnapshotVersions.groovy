/**
 * This action locks a snapshot's versions.
 */
package org.urbancode.ucadf.core.action.ucd.snapshot

import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException

class UcdLockSnapshotVersions extends UcAdfAction {
	// Action properties.
	/** The application name or ID. */
	String application
	
	/** The snapshot name or ID. */
	String snapshot
	
	/**
	 * Runs the action.	
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExist()
		
		logVerbose("Locking application [$application] snapshot [$snapshot] versions.")
		
		WebTarget target = ucdSession.getUcdWebTarget().path("/cli/snapshot/lockSnapshotVersions")
			.queryParam("snapshot", snapshot)
			.queryParam("application", application)
		logDebug("target=$target")

		Response response = target.request(MediaType.APPLICATION_JSON).put(Entity.json(""))
		if (response.getStatus() == 204) {
			logVerbose("Application [$application] snapshot [$snapshot] versions locked.")
		} else {
			throw new UcAdfInvalidValueException(response)
		}
	}
}
