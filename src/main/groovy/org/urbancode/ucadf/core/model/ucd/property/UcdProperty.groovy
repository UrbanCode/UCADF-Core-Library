/**
 * This class instantiates a property object.
 */
package org.urbancode.ucadf.core.model.ucd.property

import org.urbancode.ucadf.core.model.ucd.general.UcdObject

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class UcdProperty extends UcdObject {
	/** The property ID. */
	String id
	
	/** The name. */
	String name
	
	/** The description. */
	String description

	/** The value. */	
	String value
	
	/** The flag that indicates secure. */
	Boolean secure = false
	
	/** The flag that indicates inherited. */
	Boolean inherited = false

	// Constructors.	
	UcdProperty() {
	}
	
	UcdProperty(
		final String name, 
		final String value, 
		final String description = "", 
		final Boolean secure = false) {
		
		this.name = name
		this.value = value
		this.description = description
		this.secure = secure
	}

	/**
	 * Convert a Properties collection to a UcdProperty lsit.
	 * @param properties - The Properties collection.
	 * @return - The UcdProperty list.
	 */
	public static List<UcdProperty> propertiesToUcdProperties(Properties properties) {	
		List<UcdProperty> ucdProperties = []
		properties.each { k, v ->
			ucdProperties.add(
				new UcdProperty(k, v)
			)
		}
		
		return ucdProperties
	}
}
