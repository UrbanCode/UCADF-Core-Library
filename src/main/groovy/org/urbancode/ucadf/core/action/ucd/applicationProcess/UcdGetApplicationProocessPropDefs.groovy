/**
 * This action gets application process property definitions.
 */
package org.urbancode.ucadf.core.action.ucd.applicationProcess

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.GenericType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.applicationProcess.UcdApplicationProcess
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.general.UcdObject
import org.urbancode.ucadf.core.model.ucd.property.UcdPropDef

class UcdGetApplicationProocessPropDefs extends UcAdfAction {
	// Action properties.
	/** The application name or ID. */
	String application
	
	/** The process name or ID. */
	String process
	
	/**
	 * Runs the action.	
	 * @return The list of property definition objects.
	 */
	@Override
	public List<UcdPropDef> run() {
		// Validate the action properties.
		validatePropsExist()

		List<UcdPropDef> ucdPropDefs
				
		String processId = process
		if (!UcdObject.isUUID(process)) {
			// Must have a ID to get the full information.
			UcdApplicationProcess ucdApplicationProcess = actionsRunner.runAction([
				action: UcdGetApplicationProcess.getSimpleName(),
				application: application,
				process: process,
				failIfNotFound: true
			])
			
			processId = ucdApplicationProcess.getId()
		}

		WebTarget target = ucdSession.getUcdWebTarget().path("/rest/deploy/applicationProcess/{processId}/{version}/propDefs")
			.resolveTemplate("processId", processId)
			.resolveTemplate("version", "-1")
		logDebug("target=$target")
		
		Response response = target.request().get()
		if (response.getStatus() == 200) {
			ucdPropDefs = response.readEntity(new GenericType<List<UcdPropDef>>(){})
		} else {
			throw new UcdInvalidValueException(response)
		}

		return ucdPropDefs
	}
}