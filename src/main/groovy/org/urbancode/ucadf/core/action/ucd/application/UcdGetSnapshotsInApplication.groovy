/**
 * This action gets a list of application snapshots.
 */
package org.urbancode.ucadf.core.action.ucd.application

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.GenericType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.environment.UcdEnvironment
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.general.UcdObject
import org.urbancode.ucadf.core.model.ucd.snapshot.UcdSnapshot

class UcdGetSnapshotsInApplication extends UcAdfAction {
	// Action properties.
	/** The application name or ID. */
	String application
	
	/**
	 * Runs the action.
	 * @return The list of snapshot objects.
	 */
	public List<UcdSnapshot> run() {
		// Validate the action properties.
		validatePropsExist()

		logInfo("Getting application [$application] snapshots.")
	
		List<UcdSnapshot> snapshots
		
		WebTarget target = ucdSession.getUcdWebTarget().path("/rest/deploy/application/{application}/snapshots/active")
			.resolveTemplate("application", application)
		logDebug("target=$target")
		
		Response response = target.request().get()
		if (response.getStatus() == 200) {
			snapshots = response.readEntity(new GenericType<List<UcdSnapshot>>(){})
		} else {
            throw new UcdInvalidValueException(response)
		}
		
		return snapshots
	}
}
