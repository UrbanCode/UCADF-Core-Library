/**
 * This action gets a snapshot.
 */
package org.urbancode.ucadf.core.action.ucd.snapshot

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.snapshot.UcdSnapshot

class UcdGetSnapshot extends UcAdfAction {
	// Action properties.
	/** The application name or ID. */
	String application
	
	/** The snapshot name or ID. */
	String snapshot
	
	/** The flag that indicates fail if the snapshot is not found. Default is true. */
	Boolean failIfNotFound = true
	
	/**
	 * Runs the action.	
	 * @return The snapshot object.
	 */
	@Override
	public UcdSnapshot run() {
		// Validate the action properties.
		validatePropsExist()

		logVerbose("Getting application [$snapshot] snapshot [$snapshot].")

		UcdSnapshot ucdSnapshot
			
		WebTarget target = ucdSession.getUcdWebTarget().path("/cli/snapshot/getSnapshot")
			.queryParam("snapshot", snapshot)
			.queryParam("application", application)
		logDebug("target=$target")
		
		Response response = target.request().get()
		if (response.getStatus() == 200) {
			ucdSnapshot = response.readEntity(UcdSnapshot.class)
		} else {
			String errMsg = UcdInvalidValueException.getResponseErrorMessage(response)
			logVerbose(errMsg)
			if (response.getStatus() != 400 || failIfNotFound) {
				throw new UcdInvalidValueException(errMsg)
			}
		}
		
		return ucdSnapshot
	}
}
