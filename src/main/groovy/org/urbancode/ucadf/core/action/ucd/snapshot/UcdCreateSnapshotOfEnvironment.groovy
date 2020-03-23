/**
 * This action creates a snapshot of an environment.
 */
package org.urbancode.ucadf.core.action.ucd.snapshot

import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException

class UcdCreateSnapshotOfEnvironment extends UcAdfAction {
	// Action properties.
	/** The application name or ID. */
	String application

	/** The environment name or ID. */	
	String environment
	
	/** The snapshot name. */
	String name
	
	/** (Optional) The description. */
	String description = ""
	
	/** The flag that indicatesd exclude unmapped components. */
	Boolean excludeUnmapped = false
	
	/** The flag that indicates fail if the snapshot already exists. Default is true. */
	Boolean failIfExists = true
	
	/**
	 * Runs the action.	
	 * @return True if the snapshot was created.
	 */
	@Override
	public Boolean run() {
		// Validate the action properties.
		validatePropsExist()
		
		Boolean created = false
		
		logVerbose("Creating application [$application] snapshot [$name] with versions from environment [$environment].")
		
		WebTarget target = ucdSession.getUcdWebTarget().path("/cli/snapshot/createSnapshotOfEnvironment")
			.queryParam("environment", environment)
			.queryParam("application", application)
			.queryParam("name", name)
			.queryParam("description", description)
			.queryParam("excludeUnmapped", excludeUnmapped)
		logDebug("target=$target")
		
		Response response = target.request(MediaType.WILDCARD).accept(MediaType.APPLICATION_JSON).put(Entity.text(""))
		if (response.getStatus() == 200) {
			logVerbose("Application [$application] snapshot [$name] created of environment [$environment].")
			created = true
		} else {
			String errMsg = UcAdfInvalidValueException.getResponseErrorMessage(response)
			logVerbose(errMsg)
			if (!(response.getStatus() == 400 && (errMsg ==~ /.*already exists.*/ && !failIfExists))) {
				throw new UcAdfInvalidValueException(errMsg)
			}
		}
		
		return created
	}
}
