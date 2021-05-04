/**
 * This action gets an application property sheet.
 */
package org.urbancode.ucadf.core.action.ucd.environment

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucd.environment.UcdEnvironment
import org.urbancode.ucadf.core.model.ucd.general.UcdObject
import org.urbancode.ucadf.core.model.ucd.property.UcdPropSheet

class UcdGetEnvironmentPropSheet extends UcAdfAction {
	// Action properties.
	/** The application name or ID. */
	String application
	
	/** The environment name or ID. */
	String environment

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
		String applicationId = application
		String environmentId = environment
		
		// If UUIDs weren't provided then get the environment information.
		if (!UcdObject.isUUID(application) || !UcdObject.isUUID(environment)) {
			UcdEnvironment ucdEnvironment = actionsRunner.runAction([
				action: UcdGetEnvironment.getSimpleName(),
				actionInfo: false,
				actionVerbose: false,
				application: application,
				environment: environment,
				withDetails: true
			])
			applicationId = ucdEnvironment.getApplication().getId()
			environmentId = ucdEnvironment.getId()
		}
		
		WebTarget target = ucdSession.getUcdWebTarget()
			.path("/property/propSheet/applications&{applicationId}&environments&{environmentId}&propSheet.{versionNumber}")
			.resolveTemplate("applicationId", applicationId)
			.resolveTemplate("environmentId", environmentId)
			.resolveTemplate("versionNumber", versionNumber.toString()
		)
		logDebug("target=$target")
		
		Response response = target.request().get()
		if (response.getStatus() == 200) {
			ucdPropSheet = response.readEntity(UcdPropSheet.class)
		} else {
            throw new UcAdfInvalidValueException(response)
		}
		
		return ucdPropSheet
	}
}
