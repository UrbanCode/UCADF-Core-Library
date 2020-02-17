/**
 * This action gets the versions in a snapshot.
 */
package org.urbancode.ucadf.core.action.ucd.snapshot

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.GenericType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.action.ucd.team.UcdGetTeamUsers.ReturnAsEnum
import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.snapshot.UcdSnapshotVersions

class UcdGetSnapshotVersions extends UcAdfAction {
	/** The type of collection to return. */
	enum ReturnAsEnum {
		/** Return as a list of componentName: versionName maps. */
		LIST,
		
		/** Return as a map having the team name as the key. */
		MAPBYNAME
	}
	
	// Action properties.
	/** The application name or ID. */
	String application
	
	/** The snapshot name or ID. */
	String snapshot

	/** The type of collection to return. */
	ReturnAsEnum returnAs = ReturnAsEnum.LIST

	/**
	 * Runs the action.	
	 * @return The list of maps of version objects.
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExist()

		logVerbose("Getting application [$snapshot] snapshot [$snapshot] versions.")

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

		// Return as requested.
		Object snapshotVersions
		if (ReturnAsEnum.LIST.equals(returnAs)) {
			// Create a list of map versions.
			snapshotVersions = []
			for (entry in ucdSnapshotVersions) {
				String compName = entry.getName()
				for (version in entry.getDesiredVersions()) {
					snapshotVersions.add((compName) : version.getName())
				}
			}
		} else {
			snapshotVersions = [:]
			for (entry in ucdSnapshotVersions) {
				String compName = entry.getName()
				for (version in entry.getDesiredVersions()) {
					if (!snapshotVersions.containsKey(compName)) {
						snapshotVersions[compName] = new LinkedHashMap()

					}
					(snapshotVersions[compName] as Map).put(version.getName(), [ (compName): version.getName()] )
				}
			}
		}

		return snapshotVersions
	}
}
