/**
 * This action gets the system property sheet.
 */
package org.urbancode.ucadf.core.action.ucd.system

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucd.property.UcdPropSheet

class UcdGetSystemPropSheet extends UcAdfAction {
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
		
		WebTarget target = ucdSession.getUcdWebTarget()
			.path("/property/propSheet/system&properties.{versionNumber}")
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
