/**
 * This class instantiates a process request task object.
 */
package org.urbancode.ucadf.core.model.ucd.processRequest

import org.urbancode.ucadf.core.model.ucd.general.UcdObject
import org.urbancode.ucadf.core.model.ucd.property.UcdPropDef
import org.urbancode.ucadf.core.model.ucd.property.UcdProperty
import org.urbancode.ucadf.core.model.ucd.task.UcdTaskStatusEnum
import org.urbancode.ucadf.core.model.ucd.task.UcdTaskTypeEnum

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class UcdProcessRequestTask extends UcdObject {
	/** The process request task ID. */
	String id
	
	/** The name. */
	String name
	
	/** Flag to indicate if a comment is required. */
	Boolean commentRequired
	
	/** Flag to indicate the user can modify. */
	Boolean userCanModify
	
	/** The status. */
	UcdTaskStatusEnum status
	
	/** The start date. */
	Long startDate
	
	/** The property definitions. */
	List<UcdPropDef> propDefs
	
	/** The properties. */
	List<UcdProperty> properties

	/** The type. */	
	UcdTaskTypeEnum type
	
	/** The task completed by user name. */
	String completedBy
	
	/** The task completed on date. */
	Long completedOn
	
	/** The task completion comment. */
	String comment

	// Constructors.
	UcdProcessRequestTask() {
	}
	
	/**
	 * Get a task property value.
	 * @param propertyName The property name.
	 * @return The property value.
	 */
	public String getTaskPropertyValue(final String propertyName) {
		String value
		UcdProperty property = properties.find { it.getName() == propertyName}
		if (property) {
			value = property.getValue()
		}
		return value
	}
	
	/**
	 * Get a custom property value (prefixed with a p:) from the task.
	 * @param propertyName The property name.
	 * @return The property value.
	 */
	public String getTaskCustomPropertyValue(final String propertyName) {
		return getTaskPropertyValue("p:$propertyName")
	}
	
	/**
	 * Get all of the custom property values (prefixed with a p:).
	 * @return The property values collection.
	 */
	public Properties getTaskCustomPropertyValues() {
		Properties props = new Properties()
		properties.each {
			String propName = it.getName()
			if (propName ==~ /^p:.*/) {
				propName = propName.replaceAll(/^p:/, "")
				props.put(propName, it.getValue())
			}
		}
		
		return props
	}
}
