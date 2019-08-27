/**
 * This class instantiates cloud connection objects.
 */
package org.urbancode.ucadf.core.model.ucd.cloud

import org.urbancode.ucadf.core.model.ucd.general.UcdSecurityTypeObject

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class UcdCloudConnection extends UcdSecurityTypeObject {
	// TODO: This needs to be completed using a UCD instance that has cloud connections.
	
	// Constructors.	
	UcdCloudConnection() {
	}
}
