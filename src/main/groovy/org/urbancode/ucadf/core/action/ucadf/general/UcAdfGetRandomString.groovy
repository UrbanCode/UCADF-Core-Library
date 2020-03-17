/**
 * This action is used to get a random string with the specified number of characters.
 */
package org.urbancode.ucadf.core.action.ucadf.general

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction

class UcAdfGetRandomString extends UcAdfAction {
	// Action properties.
	/** The list of characters to use for the random string. Default is ['a'..'z','A'..'Z',0..9,'_','%','$','!'] */
	List<String> chars = ['a'..'z','A'..'Z',0..9,'_','%','$','!'].flatten()
	
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
	
		Random rand = new Random(System.currentTimeMillis())
		
		List<String> randomChars = (0..numChars-1).collect {
			chars[ rand.nextInt(chars.size()) ]
		}
		
		return randomChars.iterator().join()
	}
}
