/**
 * This class provides page control information for actions that support pages of returned rows.
 */
package org.urbancode.ucadf.core.model.ucadf

import org.urbancode.ucadf.core.model.ucd.general.UcdObject

import groovy.util.logging.Slf4j

@Slf4j
class UcAdfWaitLoopControl extends UcdObject {
	/** The default loop control property name. */
	public static LOOPCONTROLPROPERTYNAME = "waitLoopControl"
	
	/** The number of seconds to wait before retrying. */	
	Integer waitIntervalSecs
	
	/** The maximum number of seconds to wait before failing. */
	Integer maxWaitSecs
	
	/** (Optional) The maximum number of tries before failing. */
	Integer maxTries
	
	/** The dervied number of remaining seconds. */
	Integer remainingSecs
	
	/** The derived current try. */
	Integer currentTry
	
	/** The derived number of remaining tries. */
	Integer remainingTries
}
