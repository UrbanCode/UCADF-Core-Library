/**
 * This class instantiates select property definition objects.
 */
package org.urbancode.ucadf.core.model.ucd.property

import org.urbancode.ucadf.core.model.ucd.applicationProcess.UcdApplicationProcess

import com.fasterxml.jackson.annotation.JsonIgnore

class UcdPropDefSelect extends UcdPropDef {
	/** The list of allowed values. */
	List<Object> allowedValues
	
	UcdPropDefSelect() {
		this.type = TYPE_SELECT
	}
}
