/**
 * This action gets a post-processing script.
 */
package org.urbancode.ucadf.core.action.ucd.postProcessing

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.general.UcdObject
import org.urbancode.ucadf.core.model.ucd.postProcessing.UcdPostProcessingScript

class UcdGetPostProcessingScript extends UcAdfAction {
	// Action properties.
	/** The authentication script name or ID. */
	String script
	
	/** The flag that indicates fail if the authentication script is not found. Default is true. */
	Boolean failIfNotFound = true
	
	/**
	 * Runs the action.	
	 * @return The authentication script object.
	 */
	@Override
	public UcdPostProcessingScript run() {
		// Validate the action properties.
		validatePropsExist()
		
		UcdPostProcessingScript ucdPostProcessingScript

		if (UcdObject.isUUID(script)) {
			// If a UUID was provided then get the post-processing script directly.
			WebTarget target = ucdSession.getUcdWebTarget().path("/rest/script/postprocessing/{script}")
				.resolveTemplate("scriptId", script)
			logDebug("target=$target")
	
			Response response = target.request().get()
			if (response.getStatus() == 200) {
				ucdPostProcessingScript = response.readEntity(UcdPostProcessingScript)
			} else {
				String errMsg = UcdInvalidValueException.getResponseErrorMessage(response)
				logVerbose(errMsg)
				if ((response.getStatus() != 404 && response.getStatus() != 500) || failIfNotFound) {
					throw new UcdInvalidValueException(errMsg)
				}
			}
		} else {
			// Only way found to get a post-processing script by name is to get the list then find the matching one.
			List<UcdPostProcessingScript> ucdPostProcessingScripts = actionsRunner.runAction([
				action: UcdGetPostProcessingScripts.getSimpleName()
			])

			ucdPostProcessingScript = ucdPostProcessingScripts.find {
				it.getName() == script
			}
		}
		
		return ucdPostProcessingScript
	}
}
