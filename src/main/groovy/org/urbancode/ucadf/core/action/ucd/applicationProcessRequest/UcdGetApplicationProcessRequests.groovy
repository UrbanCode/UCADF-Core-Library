/**
 * This action gets a list of application process requests.
 */
package org.urbancode.ucadf.core.action.ucd.applicationProcessRequest

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.GenericType

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.applicationProcessRequest.UcdApplicationProcessRequest

class UcdGetApplicationProcessRequests extends UcAdfAction {
	// Action properties.
	/** The started after timestamp. */
	Long startedAfter
	
	/** The starting index. */
	Integer startIndex
	
	/** The flag that indicates fail if the application process request is not found. Default is true. */
	Boolean failIfNotFound = true
	
	/**
	 * Runs the action.	
	 * @return The list of application process request objects.
	 */
	@Override
	public List<UcdApplicationProcessRequest> run() {
		// Validate the action properties.
		validatePropsExistExclude([ 'startedAfter', 'startIndex' ])

		logVerbose("Getting application process requests.")

		WebTarget target = ucdSession.getUcdWebTarget().path("/cli/applicationProcessRequest")
		if (startedAfter) {
			target = target.queryParam("startedAfter", startedAfter)
		}
		if (startIndex) {
			target = target.queryParam("startIndex", startIndex)
		}
		
		logDebug("target=$target")

		return target.request().get(new GenericType<List<UcdApplicationProcessRequest>>(){})
	}
}
