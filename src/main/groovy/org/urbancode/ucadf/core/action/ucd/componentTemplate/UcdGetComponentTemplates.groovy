/**
 * This action gets a list of component templates.
 */
package org.urbancode.ucadf.core.action.ucd.componentTemplate

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.GenericType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.componentTemplate.UcdComponentTemplate
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException

class UcdGetComponentTemplates extends UcAdfAction {
	/** (Optional) If specified then gets component templates with names that match this regular expression. */
	String match = ""

	/** (Optional) If specified then gets component templates with the specified tag. */	
	String tag = ""
	
	/**
	 * Runs the action.	
	 * @return The list of component template objects.
	 */
	@Override
	public List<UcdComponentTemplate> run() {
		// Validate the action properties.
		validatePropsExist()

		List<UcdComponentTemplate> ucdComponentTemplates
		List<UcdComponentTemplate> ucdReturnComponentTemplates = []
		
		WebTarget target = ucdSession.getUcdWebTarget().path("/rest/deploy/componentTemplate")
		logDebug("target=$target")
	
		Response response = target.request().get()
		if (response.getStatus() == 200) {
			ucdComponentTemplates = response.readEntity(new GenericType<List<UcdComponentTemplate>>(){})
		} else {
            throw new UcdInvalidValueException(response)
		}
		
		if (match || tag) {		
			for (ucdComponentTemplate in ucdComponentTemplates) {
				if (match && !(ucdComponentTemplate.getName() ==~ match)) {
					continue
				}

				if (tag && !componentHasTag(ucdComponentTemplate)) {
					continue
				}
				
				ucdReturnComponentTemplates.add(ucdComponentTemplate)
			}
		} else {
			ucdReturnComponentTemplates = ucdComponentTemplates
		}
		
		return ucdReturnComponentTemplates
	}
    
	// Determine if a component has the specified tag.
	public Boolean componentHasTag(final UcdComponentTemplate ucdComponentTemplates) {
		Boolean foundTag = false

		for (ucdTag in ucdComponentTemplates.getTags()) {
			if (ucdTag.getName() == tag) {
				foundTag = true
				break
			}
		}

		return foundTag
	}
}
