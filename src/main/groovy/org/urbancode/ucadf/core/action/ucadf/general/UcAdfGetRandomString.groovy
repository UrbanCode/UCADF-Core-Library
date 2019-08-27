/**
 * This action is used to get a random string with the specified number of characters.
 */
package org.urbancode.ucadf.core.action.ucadf.general

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction

// Get the next version sequence number.
// versionPattern is of the form: *, 1.*, 1.0.*, etc.
// versions is a list of versions used to determine the last used sequence matching the pattern.
class UcAdfGetRandomString extends UcAdfAction {
	// Action properties.
	/** The number of characters for the string. */
	Integer numChars

	/**
	 * Runs the action.	
	 * @return The random string.
	 */
	@Override
	public String run() {
		// Validate the action properties.
		validatePropsExist()
	
		def pool = ['a'..'z','A'..'Z',0..9,'_','%','$','!'].flatten()
		Random rand = new Random(System.currentTimeMillis())
		
		def randomChars = (0..numChars).collect { 
			pool[ rand.nextInt(pool.size()) ] 
		}
		
		return randomChars.iterator().join()
	}
}
