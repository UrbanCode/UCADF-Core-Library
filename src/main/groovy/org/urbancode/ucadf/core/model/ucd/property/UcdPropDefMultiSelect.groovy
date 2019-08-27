/**
 * This class instantiates multi-select property definition objects.
 */
package org.urbancode.ucadf.core.model.ucd.property

import groovy.util.logging.Slf4j

@Slf4j
class UcdPropDefMultiSelect extends UcdPropDefSelect {
	UcdPropDefMultiSelect() {
		this.type = TYPE_MULTI_SELECT
	}
}
