/**
 * This action is used to iterate for a given period of time and/or retries.
 */
package org.urbancode.ucadf.core.action.ucadf.loop

import org.urbancode.ucadf.core.model.ucadf.UcAdfWaitLoopControl
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException

class UcAdfWaitLoop extends UcAdfLoop {
	/** The number of seconds to wait before retrying. */	
	Integer waitIntervalSecs
	
	/** The maximum number of seconds to wait before failing. */
	Integer maxWaitSecs
	
	/** (Optional) The maximum number of tries before failing. */
	Integer maxTries
			
	/** The counter control property name. Default is counterLoopControl. */
	String waitControlPropertyName = UcAdfWaitLoopControl.LOOPCONTROLPROPERTYNAME

	/**
	 * Runs the action.	
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExistExclude(
			[
				'waitIntervalSecs',
				'maxWaitSecs',
				'maxTries'
			]
		)

		if ((waitIntervalSecs != null && maxWaitSecs != null) || maxTries != null) {
			processWaitLoop()
		} else {
			throw new UcdInvalidValueException("Wait loop requires waitIntervalSecs and maxWaitSecs, and/or maxTries properties.")
		}
	}
	
	// Process a wait loop.
	private processWaitLoop() {
		Long startTime = new Date().getTime()
		Integer remainingSecs = maxWaitSecs
		Integer remainingTries = maxTries

		// Initialize the counter control.
		UcAdfWaitLoopControl ucAdfWaitLoopControl = new UcAdfWaitLoopControl()
		ucAdfWaitLoopControl.setWaitIntervalSecs(waitIntervalSecs)
		ucAdfWaitLoopControl.setMaxWaitSecs(maxWaitSecs)
		ucAdfWaitLoopControl.setMaxTries(maxTries)
	
		// Make the counter control object available as a property.
		actionsRunner.setPropertyValue(waitControlPropertyName, ucAdfWaitLoopControl)

		Integer currentTry = 0
		
		while(true) {
			// Set the loop control values.
			currentTry++
			ucAdfWaitLoopControl.setRemainingSecs(remainingSecs)
			ucAdfWaitLoopControl.setCurrentTry(currentTry)
			ucAdfWaitLoopControl.setRemainingTries(remainingTries)
			
			// Process the actions.
			processActions()
						
			// Stop iterating if a loop break action was true.
			if (loopBreak) {
				break
			}

			if (remainingSecs) {
				remainingSecs -= ((new Date().getTime() - startTime) / 1000)
			}
			
			if (maxTries) {
				remainingTries--
			}
			
			if (waitIntervalSecs && maxWaitSecs) {
				if (remainingSecs <= 0) {
					throw new UcdInvalidValueException("Loop wait time exceeded [$maxWaitSecs] seconds.")
				}
			}
			
			if (maxTries) {
				if (remainingTries <= 0) {
					throw new UcdInvalidValueException("Loop retries exceeded [$maxTries] tries.")
				}
			}

			logVerbose((maxWaitSecs ? "Waiting a maximum of [$maxWaitSecs] seconds with [$remainingSecs] remaining. " : "") + (maxTries ? "Maximum tries [$maxTries] with [$remainingTries] tries remaining. " : "") + ( waitIntervalSecs ? "Next try in [$waitIntervalSecs] seconds." : ""))

			processWaitInterval()
		}
		
		// Release the wait loop control object.
		actionsRunner.setPropertyValue(waitControlPropertyName, null)
	}
}
