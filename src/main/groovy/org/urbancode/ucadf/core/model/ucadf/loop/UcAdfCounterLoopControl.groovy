/**
 * This class provides page control information for actions that support pages of returned rows.
 */
package org.urbancode.ucadf.core.model.ucadf.loop

import org.urbancode.ucadf.core.model.ucd.general.UcdObject

import groovy.util.logging.Slf4j

@Slf4j
class UcAdfCounterLoopControl extends UcdObject {
	/** The default loop control property name. */
	public static LOOPCONTROLPROPERTYNAME = "counterLoopControl"
	
	/** The beginning counter value. */
	Integer counterBegin
	
	/** The counter change value. */
	Integer counterChange
	
	/** The counter end value. */
	Integer counterEnd
	
	/** The current counter value. */
	Integer counterValue
}
