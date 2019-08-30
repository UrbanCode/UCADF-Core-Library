/**
 * This class instantiates agent configuration template objects.
 */
package org.urbancode.ucadf.core.model.ucd.agent

import org.urbancode.ucadf.core.model.ucd.general.UcdSecurityTypeObject

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class UcdAgentConfigurationTemplate extends UcdSecurityTypeObject {
	// Constructors.
	UcdAgentConfigurationTemplate() {
	}
}
