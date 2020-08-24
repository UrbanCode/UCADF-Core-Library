/**
 * This action gets resource inventory entries.
 */
package org.urbancode.ucadf.core.action.ucd.resource

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucadf.loop.UcAdfPageLoopControl
import org.urbancode.ucadf.core.model.ucd.filterField.UcdFilterField
import org.urbancode.ucadf.core.model.ucd.filterField.UcdFilterFieldClassEnum
import org.urbancode.ucadf.core.model.ucd.filterField.UcdFilterFieldTypeEnum
import org.urbancode.ucadf.core.model.ucd.general.UcdObject
import org.urbancode.ucadf.core.model.ucd.resource.UcdResource
import org.urbancode.ucadf.core.model.ucd.resource.UcdResourceInventory
import org.urbancode.ucadf.core.model.ucd.resource.UcdResourceInventoryTable

class UcdGetResourceInventory extends UcAdfAction {
	/** The type of collection to return. */
	enum ReturnAsEnum {
		/** Return as a list of UcdResourceInventory objects. This is the way the UCD API returns it. */
		LIST,
		
		/** Return as a map constructed as: {"components":{"MyComp":{"component":{"id":"123456","name":"MyComp"},"versions":{"MyVersion":{"id":"234566","name":"MyVersion"}}}}} */
		MAP
	}
	
	// Action properties.
	/** The resource name or ID. */
	String resource
	
	/** (Optional) If specified then get versions with names that match this regular expression. */
	String match = ""

	/** The name of the property that has the page control information. */
	String pageControlPropertyName = UcAdfPageLoopControl.LOOPCONTROLPROPERTYNAME

	/** The flag that indicates fail if the resource is not found. Default is true. */
	Boolean failIfNotFound = true
	
	/** The type of colleciton to return. */
	ReturnAsEnum returnAs = ReturnAsEnum.MAP

	/**
	 * Runs the action.	
	 * @return The specified type of collection.
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExist()

		logDebug("Getting resource [$resource] inventory.")

		Object inventoryVersions
		
		// Construct the query fields.
		List<UcdFilterField> filterFields = []

		// If an resource ID was provided then use it. Otherwise get the resource information to get the ID.
		String resourceId = resource
		if (!UcdObject.isUUID(resource)) {
			UcdResource ucdResource = actionsRunner.runAction([
				action: UcdGetResource.getSimpleName(),
				actionInfo: false,
				actionVerbose: false,
				resource: resource,
				failIfNotFound: failIfNotFound
			])
			
			if (ucdResource) {
				resourceId = ucdResource.getId()
			} else {
				resourceId = ""
			}
		}

		if (resourceId) {
			// Add the resource ID filter field.
			filterFields.add(
				new UcdFilterField(
					"resourceId",
					resourceId,
					UcdFilterFieldTypeEnum.eq
				)
			)
	
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
	
			List<UcdResourceInventory> ucdResourceInventoryEntries = []
		
			Response response = target.request().get()
			if (response.getStatus() == 200) {
				// Get the resource inventory entries.
				UcdResourceInventoryTable ucdResourceInventoryTable = response.readEntity(UcdResourceInventoryTable.class)
				ucdResourceInventoryEntries = ucdResourceInventoryTable.getRecords()
			} else {
				throw new UcAdfInvalidValueException(response)
			}
	
			// Return the specified collection type.
			if (ReturnAsEnum.LIST.equals(returnAs)) {
				if (match) {
					List<UcdResourceInventory> resourceVersionsList = []
					for (ucdResourceInventoryEntry in ucdResourceInventoryEntries) {
						if (ucdResourceInventoryEntry.getVersion().getName() ==~ match) {
							resourceVersionsList.add(ucdResourceInventoryEntry)
						}
					}
					
					inventoryVersions = resourceVersionsList
				} else {
					inventoryVersions = ucdResourceInventoryEntries
				}
			} else {
				// Initialize the inventory versions map.
				Map<String, Map> resourceVersionsMap = [
					versions: new LinkedHashMap()
				]
		
				for (ucdResourceInventoryEntry in ucdResourceInventoryEntries) {
					String versionName = ucdResourceInventoryEntry.getVersion().getName()
					
					// Add the inventory to the version as a map item.
					if (!match || versionName ==~ match) {
						resourceVersionsMap['versions'][versionName] = ucdResourceInventoryEntry
					}
				}
				
				inventoryVersions = resourceVersionsMap
			}
		}
		
		return inventoryVersions
	}
}
