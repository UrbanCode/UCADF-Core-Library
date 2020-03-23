/**
 * This action is used to iterate over a number of items and/or for a given period of time and/or retries.
 * There are various types of loops supported and determined by the optional properties provided:
 * A counter loop.
 * A wait (polling) loop.
 * A list loop.
 * A map loop.
 */
package org.urbancode.ucadf.core.action.ucadf.loop

import org.urbancode.ucadf.core.model.ucadf.loop.UcAdfPageLoopControl

class UcAdfPageLoop extends UcAdfLoop {
	// Action properties.
	/** The number of rows per page. */	
	Integer rowsPerPage
	
	/** The page control property name. Default is pageLoopControl. */
	String pageControlPropertyName = "pageLoopControl"
			
	/**
	 * Runs the action.	
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExist()

		// Initialize the page control.
		UcAdfPageLoopControl ucAdfPageLoopControl = new UcAdfPageLoopControl()
		ucAdfPageLoopControl.setRowsPerPage(rowsPerPage)

		// Make the page control object available as a property.
		actionsRunner.setPropertyValue(pageControlPropertyName, ucAdfPageLoopControl)
		
		while(true) {		
			// Process the actions.
			processActions()
			
			// Stop iterating if a loop break action was true.
			if (loopBreak) {
				break
			}

			// If all pages processed then break.
			if (ucAdfPageLoopControl.getPageNumber() >= ucAdfPageLoopControl.getPages()) {
				break
			}

			// Increment the page number.
			ucAdfPageLoopControl.setPageNumber(ucAdfPageLoopControl.getPageNumber() + 1)
			
			// Have the option of waiting before processing the next item.
			processWaitInterval()
		}
		
		// Release the page loop control object.
		actionsRunner.setPropertyValue(pageControlPropertyName, null)
	}
}
