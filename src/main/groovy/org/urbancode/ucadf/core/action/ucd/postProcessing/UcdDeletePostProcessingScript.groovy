/**
 * This action deletes a post-processing script.
 */
package org.urbancode.ucadf.core.action.ucd.postProcessing

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.general.UcdObject
import org.urbancode.ucadf.core.model.ucd.postProcessing.UcdPostProcessingScript

class UcdDeletePostProcessingScript extends UcAdfAction {
	/** The script name or ID. */
	String script
	
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
			logVerbose("Would delete script [$script].")
		} else {
			logVerbose("Deleting script [$script].")
		
			if (UcdObject.isUUID(script)) {
				deletePostProcessingScript(script)
			} else {
				UcdPostProcessingScript ucdPostProcessingScript = actionsRunner.runAction([
					action: UcdGetPostProcessingScript.getSimpleName(),
					script: script,
					failIfNotFound: failIfNotFound
				])
				
				if (ucdPostProcessingScript) {
					deletePostProcessingScript(ucdPostProcessingScript.getId())
				}
			}
		}
		
		return deleted
	}
	
	// Delete the post-processing script .	
	public deletePostProcessingScript(final String scriptId) {
		WebTarget target = ucdSession.getUcdWebTarget().path("/rest/script/postprocessing/{scriptId}")
			.resolveTemplate("scriptId", scriptId)
		logDebug("target=$target")

		Response response = target.request().delete()
		if (response.getStatus() == 204) {
			logVerbose("Post-processing script [$scriptId] deleted.")
			deleted = true
		} else {
			String errMsg = UcdInvalidValueException.getResponseErrorMessage(response)
			logVerbose(errMsg)
			// Returns a 400 error with a 'Cannot change the ghosted date once set' for the deleted scriptes.
			if ((response.getStatus() != 400 && response.getStatus() != 404) || failIfNotFound) {
				throw new UcdInvalidValueException(errMsg)
			}
		}
	}
}
