/**
 * This action adds versions to a snapshot.
 */
package org.urbancode.ucadf.core.action.ucd.snapshot

import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException

class UcdAddVersionsToSnapshot extends UcAdfAction {
	// Action properties.
	/** The application name or ID. */
	String application
	
	/** The snapshot name or ID. */
	String snapshot
	
	/** The list of version maps, e.g. [ { "comp1" : "version1"}, {"comp2" : "version2" } ] */
	List<Map<String, String>> versions = []

	/**
	 * Runs the action.	
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExist()

		logVerbose("Adding versions to application [$application] snapshot [$snapshot].")

		for (versionItem in versions) {
			versionItem.each { component, version ->
				logVerbose("Add component [$component] version [$version] to application [$application] snapshot [$snapshot]")
				WebTarget target = ucdSession.getUcdWebTarget().path("/cli/snapshot/addVersionToSnapshot")
					.queryParam("snapshot", snapshot)
					.queryParam("application", application)
					.queryParam("version", version)
					.queryParam("component", component)
				logDebug("target=$target")
				
				Response response = target.request(MediaType.WILDCARD).accept(MediaType.APPLICATION_JSON).put(Entity.text(""))
				if (response.getStatus() == 204) {
					logVerbose("Component [$component] version [$version] added to snapshot [$snapshot].")
				} else {
					throw new UcAdfInvalidValueException(response)
				}
			}
		}
	}
}
