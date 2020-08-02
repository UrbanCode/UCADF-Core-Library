/**
 * This action gets versions.
 */
package org.urbancode.ucadf.core.action.ucd.version

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.GenericType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.action.ucd.component.UcdGetComponent
import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucadf.loop.UcAdfPageLoopControl
import org.urbancode.ucadf.core.model.ucd.component.UcdComponent
import org.urbancode.ucadf.core.model.ucd.filterField.UcdFilterField
import org.urbancode.ucadf.core.model.ucd.filterField.UcdFilterFieldClassEnum
import org.urbancode.ucadf.core.model.ucd.filterField.UcdFilterFieldTypeEnum
import org.urbancode.ucadf.core.model.ucd.general.UcdObject
import org.urbancode.ucadf.core.model.ucd.system.UcdSession
import org.urbancode.ucadf.core.model.ucd.version.UcdVersion

class UcdGetVersions extends UcAdfAction {
	/** The order fields. Default is DATECREATED. */
	enum ORDERFIELD {
		NAME("name"),
		TYPE("type"),
		USERNAME("user.name"),
		DATECREATED("dateCreated")
		
		String value
		
		ORDERFIELD(final String value) {
			this.value = value
		}
	}

	/** The sort types. Default is DESC. */
	enum SORTTYPE {
		ASC("asc"),
		DESC("desc")
		
		String value
		
		SORTTYPE(final String value) {
			this.value = value
		}
	}	
	
	// Action properties.
	/** The component name or ID. Blank by default. */
	String component = ""
	
	/** The version name or ID. Blank (all) by default. */
	String name = ""
	
	/** The version name filter type. Default is eq. */
	UcdFilterFieldTypeEnum nameFilterType = UcdFilterFieldTypeEnum.eq
	
	/** The name of the property that has the page control information. */
	String pageControlPropertyName = UcAdfPageLoopControl.LOOPCONTROLPROPERTYNAME

	/** Order field. */
	ORDERFIELD orderField = ORDERFIELD.DATECREATED

	/** Sort type. */
	SORTTYPE sortType = SORTTYPE.DESC
			
	/** The flag that indicates fail if the component is not found. Default is true. */
	Boolean failIfNotFound = true
	
	/**
	 * Runs the action.	
	 * @return The list of version objects.
	 */
	@Override
	public List<UcdVersion> run() {
		// Validate the action properties.
		validatePropsExist()

		logDebug("Getting component [$component] versions.")

		// If the page control has not been initialized then initialize it.
		UcAdfPageLoopControl ucAdfPageControl = actionsRunner.getPropertyValue(pageControlPropertyName) ?: null
		if (!ucAdfPageControl) {
			ucAdfPageControl = new UcAdfPageLoopControl()
			actionsRunner.setPropertyValue(pageControlPropertyName, ucAdfPageControl)
		}

		List<UcdVersion> ucdVersions = []
		
		// Construct the query fields.
		List<UcdFilterField> filterFields = []

		// Add a component filter.
		if (component) {
			// If a component ID was provided then use it. Otherwise get the component information to get the ID.
			String componentId = component
			if (!UcdObject.isUUID(component)) {
				UcdComponent ucdComponent = actionsRunner.runAction([
					action: UcdGetComponent.getSimpleName(),
					actionInfo: false,
					actionVerbose: false,
					component: component,
					failIfNotFound: true
				])
				componentId = ucdComponent.getId()
			}
			
			// Add the component ID filter field.
			filterFields.add(
				new UcdFilterField(
					"component.id",
					componentId,
					UcdFilterFieldTypeEnum.eq
				)
			)
		}

		// Add a version filter.
		if (name) {
			// Add the version name filter field.
			filterFields.add(
				new UcdFilterField(
					"name",
					name,
					nameFilterType
				)
			)
		}

		// Add an active-only filter.
		if (ucdSession.compareVersion(UcdSession.UCDVERSION_71) < 0) {
			filterFields.add(
				new UcdFilterField(
					"active",
					"true",
					UcdFilterFieldTypeEnum.eq,
					UcdFilterFieldClassEnum.Boolean
				)
			)
		}
		
		WebTarget target = UcdFilterField.addFilterFieldQueryParams(
			ucdSession.getUcdWebTarget().path("/rest/deploy/version"),
			filterFields
		)
		
		target = target
			.queryParam("orderField", orderField.getValue())
			.queryParam("sortType", sortType.getValue())
			.queryParam("rowsPerPage", ucAdfPageControl.getRowsPerPage())
			.queryParam("pageNumber", ucAdfPageControl.getPageNumber())
			.queryParam("outputType", "BASIC")
			.queryParam("outputType", "LINKED")

		logDebug("target=$target")

		Response response = target.request().get()
		if (response.getStatus() == 200) {
			// Get the versions.
			ucdVersions = response.readEntity(new GenericType<List<UcdVersion>>(){})
			
			// Update the page control information.
			ucAdfPageControl.processResponse(response)
		} else {
			throw new UcAdfInvalidValueException(response)
		}

		return ucdVersions
	}
}
