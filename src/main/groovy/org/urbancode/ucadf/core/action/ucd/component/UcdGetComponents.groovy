/**
 * This action gets a list of components.
 */
package org.urbancode.ucadf.core.action.ucd.component

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.GenericType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.component.UcdComponent
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException

class UcdGetComponents extends UcAdfAction {
	/** (Optional) If specified then gets components with names that match this regular expression. */
	String match = ""

	/** If true then get active components. Default is true. */	
	Boolean active = true
	
	/** If true then get inactive components. Default is false. */
	Boolean inactive = false
	
	/** (Optional) The tag name or ID for which to get components. */
	String tag = ""
	
	/**
	 * Runs the action.	
	 * @return The list of component objects.
	 */
	@Override
	public List<UcdComponent> run() {
		// Validate the action properties.
		validatePropsExist()

		List<UcdComponent> ucdComponents
		
		WebTarget target = ucdSession.getUcdWebTarget().path("/cli/component")
			.queryParam("active", active)
			.queryParam("inactive", inactive)
		logDebug("target=$target")
		
		Response response = target.request().get()
		if (response.getStatus() == 200) {
			ucdComponents = response.readEntity(new GenericType<List<UcdComponent>>(){})
		} else {
            throw new UcdInvalidValueException(response)
		}
		
		List<UcdComponent> ucdReturnComponents = []

		if (match || tag) {		
			for (ucdComponent in ucdComponents) {
				if (match && !(ucdComponent.getName() ==~ match)) {
					continue
				}

				if (tag && !componentHasTag(ucdComponent)) {
					continue
				}
				
				ucdReturnComponents.add(ucdComponent)
			}
		} else {
			ucdReturnComponents = ucdComponents
		}
		
		return ucdReturnComponents
	}
    
	// Determine if a component has the specified tag.
	public Boolean componentHasTag(final UcdComponent ucdComponent) {
		Boolean foundTag = false

		for (ucdTag in ucdComponent.getTags()) {
			if (ucdTag.getName() == tag) {
				foundTag = true
				break
			}
		}

		return foundTag
	}
}
