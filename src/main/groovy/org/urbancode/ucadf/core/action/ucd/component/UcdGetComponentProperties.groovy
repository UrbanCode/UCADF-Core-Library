/**
 * This action gets a component's properties.
 */
package org.urbancode.ucadf.core.action.ucd.component

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.GenericType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.component.UcdComponent
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.property.UcdPropSheet
import org.urbancode.ucadf.core.model.ucd.property.UcdProperty

class UcdGetComponentProperties extends UcAdfAction {
	// Action properties.
	/** The component name or ID. */
	String component

	/** If true then properties inherited from the component template are excluded. */	
	Boolean excludeInherited = false
	
	/**
	 * Runs the action.	
	 * @return The list of property objects.
	 */
	@Override
	public List<UcdProperty> run() {
		// Validate the action properties.
		validatePropsExist()

		logInfo("Getting component [$component] properties exclude inherited [$excludeInherited].")

		List<UcdProperty> ucdProperties = []
		
		if (excludeInherited) {
			UcdComponent ucdComponent = actionsRunner.runAction([
				action: UcdGetComponent.getSimpleName(),
				component: component
			])
	
			if (ucdComponent) {
				WebTarget target = ucdSession.getUcdWebTarget().path("/property/propSheet/components&{compId}&propSheetGroup&propSheets&{propSheetId}.-1")
					.resolveTemplate("compId", ucdComponent.getId())
					.resolveTemplate("propSheetId", ucdComponent.getPropSheet().getId())
				logDebug("target=$target")
				
				Response response = target.request().get()
				if (response.getStatus() == 200) {
					UcdPropSheet ucdPropSheet = response.readEntity(UcdPropSheet.class)
					
					for (ucdProperty in ucdPropSheet.getProperties()) {
						if (ucdProperty.getInherited() == false) {
							ucdProperties.add(ucdProperty)
						}
					}
				} else {
					throw new UcdInvalidValueException(response)
				}
			}
		} else {
			WebTarget target = ucdSession.getUcdWebTarget().path("/cli/component/getProperties")
				.queryParam("component", component)
			logDebug("target=$target")
	
			Response response = target.request().get()
			if (response.getStatus() == 200) {
				ucdProperties = response.readEntity(new GenericType<List<UcdProperty>>(){})
			} else {
				throw new UcdInvalidValueException(response)
			}
		}
		
		return ucdProperties
	}
}
