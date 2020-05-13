/**
 * This action gets a list of security subtypes.
 */
package org.urbancode.ucadf.core.action.ucd.security

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.GenericType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucd.security.UcdSecuritySubtype
import org.urbancode.ucadf.core.model.ucd.security.UcdSecurityType

import com.fasterxml.jackson.annotation.JsonIgnore

class UcdGetSecuritySubtypes extends UcAdfAction {
	// Action properties.
	/** (Optional) The specific type name for which to get the subtypes. If not specified then the subtypes for all types are returned. */
	String type = ""

	// Action properties.
	/** (Optional) If specified then get subtypes with names that match this regular expression. */
	String match = ""

	/** 
	 * Runs the action 
	 * @return Returns a list of security subtype objects.
	 */
	@Override
	public List<UcdSecuritySubtype> run() {
		// Validate the action properties.
		validatePropsExist()

		List<UcdSecuritySubtype> ucdSecuritySubtypes = []

		logVerbose("Getting security subtypes type [$type] match [$match].")

		if (type) {
			// Get type roles for a specific type name.
			ucdSecuritySubtypes = getResourceRoles(type)
		} else {
			// Get the list of security types.
			List<UcdSecurityType> ucdSecurityTypes = actionsRunner.runAction([
				action: UcdGetSecurityTypes.getSimpleName(),
				actionInfo: false,
				actionVerbose: false
			])

			// Add all of the security subtypes to the list.				
			for (ucdSecurityType in ucdSecurityTypes) {
				ucdSecuritySubtypes.addAll(
					getResourceRoles(ucdSecurityType.getName())
				)
			}
		}

		List<UcdSecuritySubtype> ucdReturnSecuritySubtypes = []
		
		if (match) {
			for (ucdSecuritySubtype in ucdSecuritySubtypes) {
				if (ucdSecuritySubtype.getName() ==~ match) {
					ucdReturnSecuritySubtypes.add(ucdSecuritySubtype)
				}
			}
		} else {
			ucdReturnSecuritySubtypes = ucdSecuritySubtypes
		}
		
		return ucdReturnSecuritySubtypes
	}

	// Get the resource type roles for a given resource type.
	@JsonIgnore
	public List<UcdSecuritySubtype> getResourceRoles(final String type) {
		// Initialize the list.
		List<UcdSecuritySubtype> ucdSecuritySubtypes = []

		WebTarget target = ucdSession.getUcdWebTarget().path("/security/resourceType/{type}/resourceRoles")
			.resolveTemplate("type", type)
		logDebug("target=$target")

		Response response = target.request().get()
		if (response.getStatus() == 200) {
			ucdSecuritySubtypes = response.readEntity(new GenericType<List<UcdSecuritySubtype>>(){})
		} else {
			throw new UcAdfInvalidValueException(response)
		}

		return ucdSecuritySubtypes
	}
}
