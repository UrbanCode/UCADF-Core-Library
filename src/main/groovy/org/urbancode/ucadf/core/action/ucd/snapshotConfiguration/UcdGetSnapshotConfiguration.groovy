/**
 * This action gets a snapshot configuration.
 */
package org.urbancode.ucadf.core.action.ucd.snapshotConfiguration

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.snapshotConfiguration.UcdSnapshotConfiguration

class UcdGetSnapshotConfiguration extends UcAdfAction {
	// Action properties.
	/** The application name or ID. */
	String application
	
	/** The snapshot name or ID. */
	String snapshot
	
	/** The flag that indicates fail if the snapshot is not found. Default is true. */
	Boolean failIfNotFound = true
	
	/**
	 * Runs the action.	
	 * @return The snapshot configuration object.
	 */
	@Override
	public UcdSnapshotConfiguration run() {
		// Validate the action properties.
		validatePropsExist()

		logInfo("Getting application [$snapshot] snapshot [$snapshot] configuration.")

		UcdSnapshotConfiguration ucdSnapshotConfiguration
		
		WebTarget target = ucdSession.getUcdWebTarget().path("/cli/snapshot/getSnapshotConfiguration")
			.queryParam("snapshot", snapshot)
			.queryParam("application", application)
		logDebug("target=$target")

		Response response = target.request().get()
		if (response.getStatus() == 200) {
			ucdSnapshotConfiguration = response.readEntity(UcdSnapshotConfiguration.class)
		} else {
			String errMsg = UcdInvalidValueException.getResponseErrorMessage(response)
			logInfo(errMsg)
			if (response.getStatus() != 400 || failIfNotFound) {
				throw new UcdInvalidValueException(errMsg)
			}
		}
		
		return ucdSnapshotConfiguration
	}
}
