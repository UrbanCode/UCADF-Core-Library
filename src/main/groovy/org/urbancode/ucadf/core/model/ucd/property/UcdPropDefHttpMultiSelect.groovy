/**
 * This class instantiates HTTP multi-select property definition objects.
 */
package org.urbancode.ucadf.core.model.ucd.property

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class UcdPropDefHttpMultiSelect extends UcdPropDefHttpSelect {
	UcdPropDefHttpMultiSelect() {
		this.type = TYPE_HTTP_MULTI_SELECT
	}
}
