/**
 * This action is used to iterate over a number of items and/or for a given period of time and/or retries.
 */
package org.urbancode.ucadf.core.action.ucadf.general

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.actionsrunner.UcAdfActionPropertyEnum
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException

class UcAdfLoop extends UcAdfAction {
	// Action properties.
	/** (Optional) The list or map of items to iterate. If no items are provided then this will be a polling-type loop based on time and retries. */
	Object items
	
	/** The actions to perform for each iteration. */
	List<HashMap> actions
	
	/** The name of the action property that will have the key to the current item. */
	String itemKeyProperty = "itemKey"
	
	/** The name of the action property that will have the value of the current item. */
	String itemProperty = "item"

	/** (Optional) The number of seconds to wait before retrying. */	
	Long waitIntervalSecs
	
	/** (Optional) The maximum number of seconds to wait before failing. */
	Long maxWaitSecs
	
	/** (Optional) The maximum number of tries before failing. */
	Long maxTries
	
	// Private properties.	
	private Boolean loopBreak = false
	private static String WAITINFO = 'waitInfo'

	/**
	 * Runs the action.	
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExistExclude(
			[
				'items',
				'waitIntervalSecs',
				'maxWaitSecs',
				'maxTries'
			]
		)

		if (items == null) {
			// Empty list of items was provided so must be a wait loop.
			if (!(waitIntervalSecs && maxWaitSecs) && !maxTries) {
				throw new UcdInvalidValueException("Wait loop requires waitIntervalSecs and maxWaitSecs, and/or maxTries properties.")
			}

			Long startTime = new Date().getTime()
			
	        Long remainingSecs = maxWaitSecs
			Long remainingTries = maxTries
			Long currentTry = 0

			// The item will have the wait properties.
			Map<String, Map<String, Long>> itemMap = [
				(WAITINFO): [
					waitIntervalSecs: waitIntervalSecs,
					maxWaitSecs: maxWaitSecs,
					maxTries: maxTries
				]
			]
			
			while(true) {
				// Put current wait information in the map.
				currentTry++
				itemMap[WAITINFO].put('remainingSecs', remainingSecs)
				itemMap[WAITINFO].put('remainingTries', remainingSecs)
				itemMap[WAITINFO].put('elapsedSecs', startTime - new Date().getTime())
				itemMap[WAITINFO].put('currentTry', currentTry)
				
				processItem(
					WAITINFO,
					itemMap[WAITINFO]
				)
				
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

				logInfo((maxWaitSecs ? "Waiting a maximum of [$maxWaitSecs] seconds with [$remainingSecs] remaining. " : "") + (maxTries ? "Maximum tries [$maxTries] with [$remainingTries] tries remaining. " : "") + ( waitIntervalSecs ? "Next try in [$waitIntervalSecs] seconds." : ""))

				if (waitIntervalSecs) {
					Thread.sleep(waitIntervalSecs * 1000)
				}
			}
		} else if (items instanceof List) {
			// Process a list of items.
			// For each iterator.
			for (int index = 0; index < (items as List).size(); index++) {
				// Process the list item with the key as the index number.
				processItem(
					index.toString(),
					items[index]
				)
				
				// Stop iterating if a loop break action was true.
				if (loopBreak) {
					break
				}

				// Have the option of waiting before processing the next item.
				if (waitIntervalSecs) {
					Thread.sleep(waitIntervalSecs * 1000)
				}
			}
		} else if (items instanceof Map) {
			// Process a map of items.
			// For each iterator.
			for (key in (items as Map).keySet()) {
				// Process the list item with the key as the index number.
				processItem(
					key,
					items[key]
				)
				
				// Stop iterating if a loop break action was true.
				if (loopBreak) {
					break
				}

				// Can have the option of waiting before processing the next item.
				if (waitIntervalSecs) {
					Thread.sleep(waitIntervalSecs * 1000)
				}
			}
		} else {
			throw new UcdInvalidValueException("items must be of type List or Map.")
		}
	}
	
	// Process the item.
	private processItem(
		final String itemKey,
		final Object item) {
		
		// Set the iterator property value.
		actionsRunner.setPropertyValue(itemKeyProperty, itemKey)
		actionsRunner.setPropertyValue(itemProperty, item)
		
		// Run the child actions.
		for (action in actions) {
			if (UcAdfLoopContinue.getSimpleName().equals(action.get(UcAdfActionPropertyEnum.ACTION.getPropertyName()))) {
				if (actionsRunner.runAction(action)) {
					break
				}
			} else if (UcAdfLoopBreak.getSimpleName().equals(action.get(UcAdfActionPropertyEnum.ACTION.getPropertyName()))) {
				if (actionsRunner.runAction(action)) {
					loopBreak = true
					break
				}
			} else {
				actionsRunner.runAction(action)
			}
		}
	}
}
