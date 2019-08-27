/**
 * This class is a Jersey logger.
 */
package org.urbancode.ucadf.core.integration.jersey

import groovy.util.logging.Slf4j

// Add a logger to send Jersey logging to Slf4j
@Slf4j
class JerseyLogger extends java.util.logging.Logger {
	JerseyLogger() {
		super("Jersey", null)
	}
	
	@Override public void info(String msg) { log.debug(msg) }
}
