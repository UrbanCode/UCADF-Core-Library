/**
 * This class instantiates application template tag requirement objects.
 */
package org.urbancode.ucadf.core.model.ucd.applicationTemplate

import org.urbancode.ucadf.core.model.ucadf.UcAdfObject
import org.urbancode.ucadf.core.model.ucd.tag.UcdTag

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class UcdApplicationTemplateTagRequirement extends UcAdfObject {
	/** The associated tag. */
	UcdTag tag
	
	/** The tag requirement type. */
	UcdApplicationTemplateTagRequirementTypeEnum type
	
	/** The required number of applications with the tag. */
	Integer number
}
