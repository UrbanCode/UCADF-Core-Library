/**
 * This action is used to iterate the specified number of times.
 */
package org.urbancode.ucadf.core.action.ucadf.loop

import org.urbancode.ucadf.core.model.ucadf.UcAdfCounterLoopControl

class UcAdfCounterLoop extends UcAdfLoop {
	/** The counter loop begin value. */
	Integer counterBegin
	
	/** The counter loop change value (positive or negative number). */
	Integer counterChange
	
	/** The counter loop end value (inclusive). */
	Integer counterEnd
	
	/** The counter control property name. Default is counterLoopControl. */
	String counterControlPropertyName = UcAdfCounterLoopControl.LOOPCONTROLPROPERTYNAME
	
	/**
	 * Runs the action.	
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExist()

		// Initialize the counter control.
		UcAdfCounterLoopControl ucAdfCounterLoopControl = new UcAdfCounterLoopControl()
		ucAdfCounterLoopControl.setCounterBegin(counterBegin)
		ucAdfCounterLoopControl.setCounterChange(counterChange)
		ucAdfCounterLoopControl.setCounterEnd(counterEnd)
		
		// Make the counter control object available as a property.
		actionsRunner.setPropertyValue(counterControlPropertyName, ucAdfCounterLoopControl)

		// For each counter.
		for (Integer counterValue = counterBegin; (counterEnd >= counterBegin && counterValue <= counterEnd) || (counterEnd < counterBegin && counterValue >= counterEnd) ; counterValue += counterChange) {
			// Set the current counter value.
			ucAdfCounterLoopControl.setCounterValue(counterValue)
			
			// Process the actions.
			processActions()
			
			// Stop iterating if a loop break action was true.
			if (loopBreak) {
				break
			}

			// Have the option of waiting before processing the next item.
			processWaitInterval()
		}
		
		// Release the counter loop control object.
		actionsRunner.setPropertyValue(counterControlPropertyName, null)
	}
}
