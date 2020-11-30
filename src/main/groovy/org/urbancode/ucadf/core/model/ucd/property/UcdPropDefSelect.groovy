/**
 * This class instantiates select property definition objects.
 */
package org.urbancode.ucadf.core.model.ucd.property

class UcdPropDefSelect extends UcdPropDef {
	/** The list of allowed values. */
	List<UcdPropDefSelectAllowedValue> allowedValues
	
	UcdPropDefSelect() {
		this.type = TYPE_SELECT
	}

	/**
	 * Derive a request map.
	 * @param ucdProcess
	 * @param replaceUcdPropDef
	 * @return
	 */
	@Override
	public Map deriveRequestMap(
		final Object ucdProcess,
		final UcdPropDef ucdPropDef = null) {
		
		// If no property definition was provided then this is an add so use this.
		UcdPropDefSelect replaceUcdPropDef = ucdPropDef
		if (!replaceUcdPropDef) {
			replaceUcdPropDef = this
		}
		
		// Derive the common property definition map.
        Map requestMap = super.deriveRequestMap(
			ucdProcess,
			replaceUcdPropDef
		)

		requestMap.put('allowedValues', (replaceUcdPropDef.getAllowedValues() != null) ? replaceUcdPropDef.getAllowedValues() : allowedValues)
				
		return requestMap
	}
}
