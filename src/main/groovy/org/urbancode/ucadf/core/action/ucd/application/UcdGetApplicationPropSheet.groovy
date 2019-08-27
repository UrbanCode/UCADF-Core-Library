/**
 * This action gets an application property sheet.
 */
package org.urbancode.ucadf.core.action.ucd.application

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.application.UcdApplication
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.general.UcdObject
import org.urbancode.ucadf.core.model.ucd.property.UcdPropSheet

class UcdGetApplicationPropSheet extends UcAdfAction {
	// Action properties.
	/** The application name or ID. */
	String application

	/** The property sheet version. Default is -1 (latest). */
	Integer versionNumber = -1
	
	/**
	 * Runs the action.
	 * @return The property sheet object.
	 */
	public UcdPropSheet run() {
		// Validate the action properties.
		validatePropsExist()

		UcdPropSheet ucdPropSheet
		
		// If an application ID was provided then use it. Otherwise get the application information to get the ID.
		String applicationId = application
		if (!UcdObject.isUUID(application)) {
			UcdApplication ucdApplication = actionsRunner.runAction([
				action: UcdGetApplication.getSimpleName(),
				actionInfo: false,
				application: application,
				failIfNotFound: true
			])
			applicationId = ucdApplication.getId()
		}
		
		WebTarget target = ucdSession.getUcdWebTarget()
			.path("/property/propSheet/applications&{applicationId}&propSheet.{versionNumber}")
			.resolveTemplate("applicationId", applicationId)
			.resolveTemplate("versionNumber", versionNumber.toString()
		)
		logDebug("target=$target")
		
		Response response = target.request().get()
		if (response.getStatus() == 200) {
			ucdPropSheet = response.readEntity(UcdPropSheet.class)
		} else {
            throw new UcdInvalidValueException(response)
		}
		
		return ucdPropSheet
	}
}
