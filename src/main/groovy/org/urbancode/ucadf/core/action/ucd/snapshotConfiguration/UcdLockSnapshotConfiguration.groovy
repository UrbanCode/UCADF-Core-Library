/**
 * This action locks a snapshot's configuration.
 */
package org.urbancode.ucadf.core.action.ucd.snapshotConfiguration

import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException

class UcdLockSnapshotConfiguration extends UcAdfAction {
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
		
		logInfo("Locking application [$application] snapshot [$snapshot] configuration.")
		
		WebTarget target = ucdSession.getUcdWebTarget().path("/cli/snapshot/lockSnapshotConfiguration")
			.queryParam("snapshot", snapshot)
			.queryParam("application", application)
		logDebug("target=$target")

		Response response = target.request(MediaType.APPLICATION_JSON).put(Entity.json(""))
		if (response.getStatus() == 204) {
			logInfo("Application [$application] snapshot [$snapshot] configuration locked.")
		} else {
			throw new UcdInvalidValueException(response)
		}
	}
}
