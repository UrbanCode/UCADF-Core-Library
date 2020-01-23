/**
 * This action is used to update a properties file.
 */
package org.urbancode.ucadf.core.action.ucadf.general

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction

class UcAdfUpdatePropertiesFile extends UcAdfAction {
	// Action properties.
	/** The properties file name. */
	String fileName
	
	/**
	 * Runs the action.	
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExist()
		
		List<String> lines = []
		
		File propsFile = new File(fileName)
		if (propsFile.exists()) {
			logVerbose("Reading properties from file [$fileName].")
			lines = propsFile.readLines()
		} else {
			logVerbose("Creating new properties file [$fileName].")
			propsFile.getParentFile()?.mkdirs()
		}

		List<String> setProperties = []
		
		// Process each property file line.
		for (Integer i = 0; i < lines.size(); i++) {
			// If the property file line has a property that matches one of the specified properties then replace the value.
			propertyValues.each { k, v ->
				if (lines[i] ==~ /^${k}=.*/) {
					lines[i] = "${k}=${evaluateProperty(v)}"
					
					// Save the property name in the list of properties that have been set.
					setProperties.add(k)
				}
			}
		}

		// Add any properties that weren't already set by replacement.
		propertyValues.each { k, v ->
			if (!setProperties.contains(k)) {
				lines.add("${k}=${evaluateProperty(v)}")
			}
		}		
		
		logVerbose("Writing properties file [$fileName].")
		propsFile.withWriter { out ->
		  lines.each { out.println it }
		}	
	}

	// Use the actions runner set/get property valeu to evaluate the property and do any variable replacement.
	private evaluateProperty(final String value) {
		actionsRunner.setPropertyValue("temp", value)
		return actionsRunner.getPropertyValue("temp")
	}
}
