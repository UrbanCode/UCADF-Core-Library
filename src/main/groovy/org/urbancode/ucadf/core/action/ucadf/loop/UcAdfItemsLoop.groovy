/**
 * This action is used to iterate over a number of items.
 */
package org.urbancode.ucadf.core.action.ucadf.loop

import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException

class UcAdfItemsLoop extends UcAdfLoop {
	// Action properties.
	/** The list or map of items to iterate. If no items are provided then this will be a polling-type loop based on time and retries. */
	Object items
	
	/** The name of the action property that will have the key to the current item. */
	String itemKeyProperty = "itemKey"
	
	/** The name of the action property that will have the value of the current item. */
	String itemProperty = "item"
	
	/**
	 * Runs the action.	
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExist()

		if (items instanceof List) {
			processListLoop()
		} else if (items instanceof Map) {
			processMapLoop()
		} else {
			throw new UcAdfInvalidValueException("items must be of type List or Map.")
		}
	}
	
	// Process a list of items.
	private processListLoop() {
		// For each iterator.
		for (int index = 0; index < (items as List).size(); index++) {
			// Set the iterator property values.
			actionsRunner.setPropertyValue(itemKeyProperty, index.toString())
			actionsRunner.setPropertyValue(itemProperty, items[index])

			// Process the actions.
			processActions()
			
			// Stop iterating if a loop break action was true.
			if (loopBreak) {
				break
			}

			// Have the option of waiting before processing the next item.
			processWaitInterval()
		}
	}
	
	// Process a map of items.
	private processMapLoop() {
		// For each iterator.
		for (key in (items as Map).keySet()) {
			// Set the iterator property values.
			actionsRunner.setPropertyValue(itemKeyProperty, key)
			actionsRunner.setPropertyValue(itemProperty, items[key])

			// Process the actions.
			processActions()
			
			// Stop iterating if a loop break action was true.
			if (loopBreak) {
				break
			}

			// Can have the option of waiting before processing the next item.
			processWaitInterval()
		}
	}	
}
