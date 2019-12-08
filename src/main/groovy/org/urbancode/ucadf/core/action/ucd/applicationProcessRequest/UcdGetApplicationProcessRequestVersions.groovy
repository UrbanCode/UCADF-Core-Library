/**
 * This action gets an application process request's versions.
 */
package org.urbancode.ucadf.core.action.ucd.applicationProcessRequest

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.applicationProcessRequest.UcdApplicationProcessRequestVersions
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.version.UcdVersionTypeEnum

class UcdGetApplicationProcessRequestVersions extends UcAdfAction {
	/** The type of collection to return. */
	enum ReturnAsEnum {
		/** Return as a list of UcdVersion objects. This is the way the UCD API returns it. */
		LIST,
		
		/** Return as a map constructed as: {"components":{"MyComp":{"component":{"id":"123456","name":"MyComp"},"versions":{"MyVersion":{"id":"234566","name":"MyVersion"}}}}} */
		MAP,
		
		/** Return as a map constructed as: {"components":{"MyComp":{"component":{"id":"123456","name":"MyComp"},"versions":[{"id":"234566","name":"MyVersion"}]}}} */
		MAPLIST
	}

	// Action properties.
	/** The application process request ID. */
	String requestId
	
	/** This flag indicates to validate that only one full version of each component is selected. */
	Boolean validateSingleFullVersions = false
	
	/** The flag that indicates fail if the application process request is not found. Default is true. */
	Boolean failIfNotFound = true

	/** The type of colleciton to return. */
	ReturnAsEnum returnAs = ReturnAsEnum.MAP

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
			// API call returns a list of version objects and for each of the associated component objects.
			ucdApplicationProcessRequestVersions = response.readEntity(UcdApplicationProcessRequestVersions.class)

			// Process the list of versions to create a list of maps.
			Boolean hasMultiple = false
			for (ucdVersion in ucdApplicationProcessRequestVersions.getVersions()) {
				String compName = ucdVersion.getComponent().getName()
				String versionName = ucdVersion.getName()

				println "Request has component [$compName] version [$versionName]."

				if (requestVersionsMap['components'].containsKey(compName)) {
					if (ucdVersion.getType() == UcdVersionTypeEnum.FULL && validateSingleFullVersions) {
						logError("Component [$compName] has more than one version selected.")
						hasMultiple = true
					}
				} else {
					// Add the component to the components map.
					requestVersionsMap['components'][compName] = [
						component: [
							id: ucdVersion.getComponent().getId(),
							name: compName
						]
					]
					
					if (ReturnAsEnum.MAPLIST.equals(returnAs)) {
						requestVersionsMap['components'][compName]['versions'] = []
					} else {
						requestVersionsMap['components'][compName]['versions'] = new LinkedHashMap()
					}
				}
				
				// Add the version to the component versions map.
				if (ReturnAsEnum.MAPLIST.equals(returnAs)) {
					// Add the version as a list item.
					(requestVersionsMap['components'][compName]['versions'] as List).add(
						[
							id: ucdVersion.getId(),
							name: versionName
						]
					)
				} else {
					// Add the version as a map item.
					requestVersionsMap['components'][compName]['versions'][versionName] = [
						id: ucdVersion.getId(),
						name: versionName
					]
				}
			}
			
			if (validateSingleFullVersions && hasMultiple) {
				throw new UcdInvalidValueException("A given component is not allowed to have more than one full version selected.")
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
