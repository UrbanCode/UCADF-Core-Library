/**
 * This action gets the UCD instance version.
 */
package org.urbancode.ucadf.core.action.ucd.system

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction

class UcdGetUcdVersion extends UcAdfAction {
	/**
	 * Runs the action.	
	 */
	@Override
	public Object run() {
		return ucdSession.getUcdVersion()
    }
}
