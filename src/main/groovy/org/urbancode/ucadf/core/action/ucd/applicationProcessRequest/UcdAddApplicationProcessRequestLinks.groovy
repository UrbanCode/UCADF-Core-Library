/**
 * This action adds links to an application process request.
 */
package org.urbancode.ucadf.core.action.ucd.applicationProcessRequest

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.applicationProcessRequest.UcdApplicationProcessRequestLink
import org.urbancode.ucadf.core.model.ucd.property.UcdProperty

class UcdAddApplicationProcessRequestLinks extends UcAdfAction {
	// Action properties.
	/** The application process request ID. */
	String requestId
	
	/** The list of links to add. */
	List<UcdApplicationProcessRequestLink> links = []
	
	/**
	 * Runs the action.	
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExist()

		// Construct a list of properties for the links.
		List<UcdProperty> ucdProperties = []
		for (link in links) {
			ucdProperties.add(
				new UcdProperty("link:${link.getTitle()}", link.getValue())
			)
		}

		// Add the link properties to the application process request.		
		actionsRunner.runAction([
			action: UcdSetApplicationProcessRequestProperties.getSimpleName(),
			requestId: requestId,
			properties: ucdProperties
		])
	}
}
