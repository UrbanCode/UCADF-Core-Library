/**
 * This action gets the component's versions.
 */
package org.urbancode.ucadf.core.action.ucd.version

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.GenericType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.version.UcdVersion

class UcdGetComponentVersions extends UcAdfAction {
	// Action properties.
	/** The component name or ID. */
	String component
	
	/** The flag that indicates include inactive versions. */
	Boolean inactive = false

	/** The number of results to return. A value greater than 0 limits the results. Default is -1 (all versions). */
	Integer numResults = -1

	/** (Optional) If specified then get versions with names that match this regular expression. */
	String match = ""

	/** The flag that indicates fail if the component is not found. Default is true. */
	Boolean failIfNotFound = true
	
	/**
	 * Runs the action.	
	 * @return The list of version objects.
	 */
	@Override
	public List<UcdVersion> run() {
		// Validate the action properties.
		validatePropsExist()

		logDebug("Getting component [$component] versions.")
	
		List<UcdVersion> ucdVersions = []
		
		WebTarget target = ucdSession.getUcdWebTarget().path("/cli/component/versions")
			.queryParam("component", component)
			.queryParam("inactive", inactive)
			.queryParam("numResults", numResults)
		logDebug("target=$target")
		
		Response response = target.request().get()
		if (response.getStatus() == 200) {
			ucdVersions = response.readEntity(new GenericType<List<UcdVersion>>(){})
		} else {
			String errMsg = UcdInvalidValueException.getResponseErrorMessage(response)
			logInfo(errMsg)
			if (response.getStatus() == 404) {
				if (failIfNotFound) {
					throw new UcdInvalidValueException(errMsg)
				}
			} else {
				throw new UcdInvalidValueException(errMsg)
			}
		}
		
		List<UcdVersion> ucdReturnVersions = []
		
		if (match) {
			for (ucdVersion in ucdVersions) {
				if (ucdVersion.getName() ==~ match) {
					ucdReturnVersions.add(ucdVersion)
				}
			}
		} else {
			ucdReturnVersions = ucdVersions
		}
		
		return ucdReturnVersions
	}
}
