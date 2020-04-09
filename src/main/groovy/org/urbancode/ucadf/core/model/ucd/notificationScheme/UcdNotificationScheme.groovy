/**
 * This class instantiates lock objects.
 */
package org.urbancode.ucadf.core.model.ucd.notificationScheme

import org.urbancode.ucadf.core.model.ucd.general.UcdObject

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class UcdNotificationScheme extends UcdObject {
	String id
	String name
	String description
	
	// Constructors.
	UcdNotificationScheme() {
	}
}
