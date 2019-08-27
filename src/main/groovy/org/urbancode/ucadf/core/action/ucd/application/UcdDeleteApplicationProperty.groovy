/**
 * This action deletes an application property.
 */
package org.urbancode.ucadf.core.action.ucd.application

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.application.UcdApplication
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.general.UcdObject
import org.urbancode.ucadf.core.model.ucd.property.UcdPropSheet

class UcdDeleteApplicationProperty extends UcAdfAction {
	// Action properties.
	/** The application name or ID. */
	String application
	
	/** The property name. */
	String property
	
	/** The flag that indicates fail if the application is not found. Default is false. */
	Boolean failIfNotFound = false
	
	/**
	 * Runs the action.
	 * @return True if the application property was deleted.
	 */
	public Boolean run() {
		// Validate the action properties.
		validatePropsExist()

		Boolean deleted = false
		
		logInfo("Deleting application [$application] property [$property].")

		// If an application ID was provided then use it. Otherwise get the application information to get the ID.
		String applicationId = application
		if (!UcdObject.isUUID(application)) {
			UcdApplication ucdApplication = actionsRunner.runAction([
				action: UcdGetApplication.getSimpleName(),
				application: application,
				failIfNotFound: true
			])
			applicationId = ucdApplication.getId()
		}
		
		// Get the propSheet so we can get the current version number from it.
		UcdPropSheet ucdPropSheet = actionsRunner.runAction([
			action: UcdGetApplicationPropSheet.getSimpleName(),
			application: applicationId
		])

		WebTarget target = ucdSession.getUcdWebTarget()
			.path("/property/propSheet/applications&{applicationId}&propSheet.-1/propValues/{propertyName}")
			.resolveTemplate("applicationId", applicationId)
			.resolveTemplate("propertyName", property
		)
		logDebug("target=$target")

		Response response = target.request().header("Version", ucdPropSheet.getVersion()).delete()
		if (response.getStatus() == 200) {
			logInfo("Application [$application] property [$property] deleted.")
			deleted = true
		} else {
            throw new UcdInvalidValueException(response)
		}
		
		return deleted
	}
}
