/**
 * This action gets a version's links.
 */
package org.urbancode.ucadf.core.action.ucd.version

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.GenericType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucd.version.UcdVersion
import org.urbancode.ucadf.core.model.ucd.version.UcdVersionLink

class UcdGetVersionLinks extends UcAdfAction {
	/** The type of collection to return. */
	enum ReturnAsEnum {
		/** Return as List<UcdProperty>. */
		LIST,
		
		/** Return as Map<String, UcdProperty> having the property name as the key. */
		MAPBYNAME
	}
	
	// Action properties.
	/** The component name or ID. */
	String component
	
	/** The version name or ID. */
	String version
	
	/** The type of collection to return. */
	ReturnAsEnum returnAs = ReturnAsEnum.MAPBYNAME
	
	/** The flag that indicates fail if the version is not found. Default is true. */
	Boolean failIfNotFound = true
	
	/**
	 * Runs the action.	
	 * @return The specified type of collection.
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExist()

		List<UcdVersionLink> ucdVersionLinks = []
		
		logVerbose("Getting component [$component] version [$version] links.")

		// Work around 7.0 bug where it converts a version name with 4 hyphens to a UUID.
		if (isIncorrectlyInterpretedAsUUID(version)) {
			UcdVersion ucdVersion = actionsRunner.runAction([
				action: UcdGetVersion.getSimpleName(),
				actionInfo: false,
				actionVerbose: false,
				component: component,
				version: version,
				failIfNotFound: true
			])
			
			version = ucdVersion.getId()
		}

		WebTarget target = ucdSession.getUcdWebTarget().path("/cli/version/getLinks")
			.queryParam("component", component)
			.queryParam("version", version)
		logDebug("target=$target")

		Response response = target.request().get()
		if (response.getStatus() == 200) {
			ucdVersionLinks = response.readEntity(new GenericType<List<UcdVersionLink>>(){})
		} else {
			if (response.getStatus() != 404 || failIfNotFound) {
				throw new UcAdfInvalidValueException(response)
			}
		}

		// Return as requested.
		Object versionLinks
		if (ReturnAsEnum.LIST.equals(returnAs)) {
			versionLinks = ucdVersionLinks
		} else {
			versionLinks = [:]
			for (ucdProperty in ucdVersionLinks) {
				versionLinks.put(ucdProperty.getName(), ucdProperty)
			}
		}

		return versionLinks
	}
}
