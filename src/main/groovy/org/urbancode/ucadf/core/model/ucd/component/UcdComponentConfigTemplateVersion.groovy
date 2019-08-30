/**
 * This class instantiates component configuration template objects.
 */
package org.urbancode.ucadf.core.model.ucd.component

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class UcdComponentConfigTemplateVersion extends UcdComponentConfigTemplate {
	/** The associated component. */
	UcdComponent component
	
	// Constructors.	
	UcdComponentConfigTemplateVersion() {
	}
}
