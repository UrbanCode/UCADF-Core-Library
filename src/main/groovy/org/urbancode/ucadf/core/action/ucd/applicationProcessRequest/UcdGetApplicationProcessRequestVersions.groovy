/**
 * This action gets an application process request's versions.
 */
package org.urbancode.ucadf.core.action.ucd.applicationProcessRequest

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.applicationProcessRequest.UcdApplicationProcessRequestVersions
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException

class UcdGetApplicationProcessRequestVersions extends UcAdfAction {
	/** The type of collection to return. */
	enum ReturnAsEnum {
		/** Return as a list. This is the way the UCD API returns it. */
		LIST,
		
		/** Return as a map having the TODO: as the key. */
		MAP
	}

	// Action properties.
	/** The application process request ID. */
	String requestId
	
	/** If true then validate that each component has only a single version. */
	Boolean validateSingle = false
	
	/** The flag that indicates fail if the application process request is not found. Default is true. */
	Boolean failIfNotFound = true

	/** The type of colleciton to return. */
	ReturnAsEnum returnAs = ReturnAsEnum.MAP

	// TODO: Change the returned map to a defined collection class?
		
	/**
	 * Runs the action.	
	 * @return The specified type of collection.
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExist()

		logInfo("Getting application process request [$requestId] versions as [$returnAs].")
		
		// Initialize the request versions list.		
		List<Map<String, String>> requestVersionsList = []
		
		// Initialize the request versions map.
		Map<String, Map> requestVersionsMap = [
			components: new LinkedHashMap()
		]
		
		WebTarget target = ucdSession.getUcdWebTarget().path("/rest/deploy/applicationProcessRequest/{requestId}/versions")
			.resolveTemplate("requestId", requestId)
		logDebug("target=$target")
		
		Response response = target.request().get()
		UcdApplicationProcessRequestVersions ucdApplicationProcessRequestVersions
		if (response.status == 200) {
			// API call returns a list of version objects and for each the associated component object.
			ucdApplicationProcessRequestVersions = response.readEntity(UcdApplicationProcessRequestVersions.class)

			// Process the list of versions to create a list of maps.
			Boolean hasMultiple = false
			for (ucdVersion in ucdApplicationProcessRequestVersions.getVersions()) {
				String compName = ucdVersion.getComponent().getName()
				String versionName = ucdVersion.getName()

				println "Request has component [$compName] version [$versionName]."
				
				if (requestVersionsMap['components'].containsKey(compName)) {
					if (validateSingle) {
						logError("Component [$compName] has more than one version selected.")
						hasMultiple = true
					}
				} else {
					// Add the component to the components map.
					requestVersionsMap['components'][compName] = [
						component: [
							id: ucdVersion.getComponent().getId(),
							name: compName
						],
						versions: new LinkedHashMap()
					]
				}
				
				// Add the version to the component versions map.
				requestVersionsMap['components'][compName]['versions'][versionName] = [
					id: ucdVersion.getId(),
					name: versionName
				]
			}
			
			if (validateSingle && hasMultiple) {
				throw new UcdInvalidValueException("A given component is not allowed to have more than one version selected.")
			}
		} else {
			String errMsg = UcdInvalidValueException.getResponseErrorMessage(response)
			logInfo(errMsg)
			if (response.getStatus() == 400 || response.status == 404) {
				if (failIfNotFound) {
					throw new UcdInvalidValueException(errMsg)
				}
			} else {
				throw new UcdInvalidValueException(errMsg)
			}
		}

		// Return the specified collection type.
		Object requestVersions
		if (ReturnAsEnum.LIST.equals(returnAs)) {
			if (ucdApplicationProcessRequestVersions) {
				requestVersions = ucdApplicationProcessRequestVersions.getVersions()
			}
		} else {
			requestVersions = requestVersionsMap
		}

		return requestVersions
	}
}
