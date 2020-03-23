/**
 * This action deletes a system property.
 */
package org.urbancode.ucadf.core.action.ucd.system

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucd.property.UcdPropSheet

class UcdDeleteSystemProperty extends UcAdfAction {
	/** The system property name. */
	String property
	
	/** The flag that indicates to perform the delete, otherwise show that the delete would be done. Default is true. */
	Boolean commit = true
	
	/** The flag that indicates fail if the script is not found. Default is false. */
	Boolean failIfNotFound = false
	
	// Private properties.
	private Boolean deleted = false
	
	/**
	 * Runs the action.	
	 * @return True if the script was deleted.
	 */
	@Override
	public Boolean run() {
		// Validate the action properties.
		validatePropsExist()

		Boolean deleted = false
		
		if (!commit) {
			logVerbose("Would delete system property [$property].")
		} else {
			logVerbose("Deleting system property [$property].")

			if (property) {		
				// Get the propSheet so we can get the current version number from it.
				UcdPropSheet ucdPropSheet = actionsRunner.runAction([
					action: UcdGetSystemPropSheet.getSimpleName()
				])

				WebTarget target = ucdSession.getUcdWebTarget()
					.path("/property/propSheet/system&properties.-1/propValues/{propertyName}")
					.resolveTemplate("propertyName", property)
				logDebug("target=$target")

				Response response = target.request().header("version", ucdPropSheet.getVersion()).delete()
				if (response.getStatus() == 200) {
					logVerbose("System property [$property] deleted.")
					deleted = true
				} else {
		            throw new UcAdfInvalidValueException(response)
				}
			} else {
				logVerbose("Can't delete property with name [$property]")
			}
		}
		
		return deleted
	}
}
