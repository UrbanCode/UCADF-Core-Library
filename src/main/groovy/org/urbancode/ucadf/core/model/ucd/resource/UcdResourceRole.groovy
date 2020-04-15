/**
 * This class instantiates resource objects.
 */
package org.urbancode.ucadf.core.model.ucd.resource

import org.urbancode.ucadf.core.model.ucd.general.UcdObject

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class UcdResourceRole extends UcdObject {
	String id
	String name
	String specialType
}
