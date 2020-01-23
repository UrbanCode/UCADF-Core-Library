/**
 * This action gets a component configuration teamplate version.
 */
package org.urbancode.ucadf.core.action.ucd.componentConfigTemplate

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.GenericType
import javax.ws.rs.core.Response
import org.urbancode.ucadf.core.action.ucd.component.UcdGetComponent
import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.component.UcdComponent
import org.urbancode.ucadf.core.model.ucd.component.UcdComponentConfigTemplateVersion
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.general.UcdObject

class UcdGetComponentConfigTemplateVersion extends UcAdfAction {
	// Action properties.
	/** The component name or ID. */
	String component

	/** The component configuration template name or ID. */	
	String configTemplate
	
	/** The component configuration template version. */
	Long version
	
	/** The flag that indicates fail if the component configuration template is not found. Default is true. */
	Boolean failIfNotFound = true
	
	/**
	 * Runs the action.	
	 * @return The component configuration template version.
	 */
	@Override
	public UcdComponentConfigTemplateVersion run() {
		// Validate the action properties.
		validatePropsExist()

		UcdComponentConfigTemplateVersion configFileTemplateVersion
		
		logVerbose("Getting component [$component] configuration template [$configTemplate] version [$version].")

		// If an component ID was provided then use it. Otherwise get the component information to get the ID.
		String componentId = component
		if (!UcdObject.isUUID(component)) {
			UcdComponent ucdComponent = actionsRunner.runAction([
				action: UcdGetComponent.getSimpleName(),
				component: component,
				failIfNotFound: true
			])
			
			componentId = ucdComponent.getId()
		}
		
		WebTarget target = ucdSession.getUcdWebTarget().path("/rest/deploy/configTemplate/{component}/{templateName}/{version}")
			.resolveTemplate("component", componentId)
			.resolveTemplate("templateName", configTemplate)
			.resolveTemplate("version", version.toString())
		logDebug("target=$target")
		
		Response response = target.request().get()
		if (response.getStatus() == 200) {
			configFileTemplateVersion = response.readEntity(new GenericType<UcdComponentConfigTemplateVersion>(){})
		} else {
			String errMsg = UcdInvalidValueException.getResponseErrorMessage(response)
			logVerbose(errMsg)
			if (response.getStatus() != 404 || failIfNotFound) {
				throw new UcdInvalidValueException(errMsg)
			}
		}
		
		return configFileTemplateVersion
	}	
}
