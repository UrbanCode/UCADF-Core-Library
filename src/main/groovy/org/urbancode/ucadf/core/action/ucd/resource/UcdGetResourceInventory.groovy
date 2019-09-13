/**
 * This action gets resource inventory entries.
 */
package org.urbancode.ucadf.core.action.ucd.resource

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.UcAdfPageLoopControl
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.filterField.UcdFilterField
import org.urbancode.ucadf.core.model.ucd.filterField.UcdFilterFieldClassEnum
import org.urbancode.ucadf.core.model.ucd.filterField.UcdFilterFieldTypeEnum
import org.urbancode.ucadf.core.model.ucd.general.UcdObject
import org.urbancode.ucadf.core.model.ucd.resource.UcdResource
import org.urbancode.ucadf.core.model.ucd.resource.UcdResourceInventory
import org.urbancode.ucadf.core.model.ucd.resource.UcdResourceInventoryTable

class UcdGetResourceInventory extends UcAdfAction {
	// Action properties.
	/** The resource name or ID. */
	String resource
	
	/** The name of the property that has the page control information. */
	String pageControlPropertyName = UcAdfPageLoopControl.LOOPCONTROLPROPERTYNAME

	/** The flag that indicates fail if the resource is not found. Default is true. */
	Boolean failIfNotFound = true
	
	/**
	 * Runs the action.	
	 * @return The list of resource inventory objects.
	 */
	@Override
	public List<UcdResourceInventory> run() {
		// Validate the action properties.
		validatePropsExist()

		logDebug("Getting resource [$resource] inventory.")

		List<UcdResourceInventory> ucdResourceInventoryEntries = []
		
		// Construct the query fields.
		List<UcdFilterField> filterFields = []

		if (resource) {
			// If an resource ID was provided then use it. Otherwise get the resource information to get the ID.
			String resourceId = resource
			if (!UcdObject.isUUID(resource)) {
				UcdResource ucdResource = actionsRunner.runAction([
					action: UcdGetResource.getSimpleName(),
					actionInfo: false,
					resource: resource,
					failIfNotFound: true
				])
				resourceId = ucdResource.getId()
			}

			// Add the resource ID filter field.
			filterFields.add(
				new UcdFilterField(
					"resourceId",
					resourceId,
					UcdFilterFieldTypeEnum.eq
				)
			)
		}

		filterFields.add(
			new UcdFilterField(
				"ghostedDate",
				"0",
				UcdFilterFieldTypeEnum.eq,
				UcdFilterFieldClassEnum.Long
			)
		)
		
		WebTarget target = UcdFilterField.addFilterFieldQueryParams(
			ucdSession.getUcdWebTarget().path("/rest/inventory/resourceInventory/table"),
			filterFields
		)
		
		logDebug("target=$target")

		Response response = target.request().get()
		if (response.getStatus() == 200) {
			// Get the resource inventory entries.
			UcdResourceInventoryTable ucdResourceInventoryTable = response.readEntity(UcdResourceInventoryTable.class)
			ucdResourceInventoryEntries = ucdResourceInventoryTable.getRecords()
		} else {
			throw new UcdInvalidValueException(response)
		}

		return ucdResourceInventoryEntries
	}
}
