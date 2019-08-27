/**
 * This action gets the versions in a snapshot.
 */
package org.urbancode.ucadf.core.action.ucd.snapshot

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.GenericType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.snapshot.UcdSnapshotVersions

class UcdGetSnapshotVersions extends UcAdfAction {
	// Action properties.
	/** The application name or ID. */
	String application
	
	/** The snapshot name or ID. */
	String snapshot
	
	/**
	 * Runs the action.	
	 * @return The list of maps of version objects.
	 */
	@Override
	public List<Map<String, String>> run() {
		// Validate the action properties.
		validatePropsExist()

		logInfo("Getting application [$snapshot] snapshot [$snapshot] versions.")

		List<Map<String, String>> snapshotVersions = []
		
		List<UcdSnapshotVersions> ucdSnapshotVersions = []
		
		WebTarget target = ucdSession.getUcdWebTarget().path("/cli/snapshot/getSnapshotVersions")
			.queryParam("snapshot", snapshot)
			.queryParam("application", application)
		logDebug("target=$target")
		
		Response response = target.request().get()
		if (response.getStatus() == 200) {
			ucdSnapshotVersions = response.readEntity(new GenericType<List<UcdSnapshotVersions>>(){})
		} else {
			throw new UcdInvalidValueException(response)
		}

		// Create a list of map versions.
		for (entry in ucdSnapshotVersions) {
			String compName = entry.getName()
			for (version in entry.getDesiredVersions()) {
				snapshotVersions.add((compName) : version.getName())
			}
		}

		return snapshotVersions
	}
}
