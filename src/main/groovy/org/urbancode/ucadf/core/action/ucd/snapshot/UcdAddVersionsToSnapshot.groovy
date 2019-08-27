/**
 * This action adds versions to a snapshot.
 */
package org.urbancode.ucadf.core.action.ucd.snapshot

import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.snapshot.UcdSnapshot
import org.urbancode.ucadf.core.model.ucd.version.UcdVersion
import groovy.util.logging.Slf4j;

class UcdAddVersionsToSnapshot extends UcAdfAction {
	// Action properties.
	/** The application name or ID. */
	String application
	
	/** The snapshot name or ID. */
	String snapshot
	
	/** The list of versions. */
	List<UcdVersion> versions

	/**
	 * Runs the action.	
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExist()

		logInfo("Adding versions to application [$application] snapshot [$snapshot].")

		for (ucdVersion in versions) {
			String component = ucdVersion.getComponent()
			String version = ucdVersion.getName()
			
			logInfo("Add component [$component] version [$version] to application [$application] snapshot [$snapshot]")
			WebTarget target = ucdSession.getUcdWebTarget().path("/cli/snapshot/addVersionToSnapshot")
				.queryParam("snapshot", snapshot)
				.queryParam("application", application)
				.queryParam("version", version)
				.queryParam("component", component)
			logDebug("target=$target")
			
			Response response = target.request(MediaType.WILDCARD).accept(MediaType.APPLICATION_JSON).put(Entity.text(""))
			if (response.getStatus() == 204) {
				logInfo("Component [$component] version [$version] added to snapshot [$snapshot].")
			} else {
				throw new UcdInvalidValueException(response)
			}
		}
	}
}
