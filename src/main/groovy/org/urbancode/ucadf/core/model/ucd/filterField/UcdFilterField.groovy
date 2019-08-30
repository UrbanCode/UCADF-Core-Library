/**
 * This class instantiates a filter field.
 */
package org.urbancode.ucadf.core.model.ucd.filterField

import javax.ws.rs.client.WebTarget

import org.urbancode.ucadf.core.model.ucd.general.UcdObject

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class UcdFilterField extends UcdObject {
	/** The field name. */	
	String fieldName
	
	/** The field value. */
	String fieldValue
	
	/** The field comparision type. */
	UcdFilterFieldTypeEnum fieldType
	
	/** The field class type. */
	UcdFilterFieldClassEnum fieldClass

	// Constructros.
	UcdFilterField() {
	}
	
	UcdFilterField(
		final String fieldName,
		final String fieldValue,
		final UcdFilterFieldTypeEnum fieldType,
		final UcdFilterFieldClassEnum fieldClass = UcdFilterFieldClassEnum.String) {
		
		this.fieldName = fieldName
		this.fieldValue = fieldValue
		this.fieldType = fieldType
		this.fieldClass = fieldClass
	}

	/**
	 * Add filter field query parameters to a web target.
	 * @param target The web target.
	 * @param filterFields The list of filter fields.
	 * @return The web target with the filter field query parameters added.
	 */
	public static WebTarget addFilterFieldQueryParams(
		WebTarget target,
		final List<UcdFilterField> filterFields) {
		
		for (filterField in filterFields) {
			String fieldName = filterField.getFieldName()
			target = target.queryParam("filterFields", fieldName)
			target = target.queryParam("filterValue_${fieldName}", filterField.getFieldValue())
			target = target.queryParam("filterType_${fieldName}", filterField.getFieldType())
			target = target.queryParam("filterClass_${fieldName}", filterField.getFieldClass())
		}
		
		return target
	}	
}
