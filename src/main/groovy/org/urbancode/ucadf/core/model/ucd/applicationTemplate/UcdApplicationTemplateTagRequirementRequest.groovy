/**
 * This action adds an application template to teams/subtypes.
 */
package org.urbancode.ucadf.core.model.ucd.applicationTemplate

import org.urbancode.ucadf.core.model.ucadf.UcAdfObject

class UcdApplicationTemplateTagRequirementRequest extends UcAdfObject {
	String name
	UcdApplicationTemplateTagRequirementTypeEnum type
	Integer number
	
	// Constructors.
	UcdApplicationTemplateTagRequirementRequest(
		final String name, 
		final UcdApplicationTemplateTagRequirementTypeEnum type, 
		final Integer number) {

		this.name = name
		this.type = type
		this.number = number			
	}
}
