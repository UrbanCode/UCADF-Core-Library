/**
 * This action is used to get the next sequential version number based on the list of versions and the version pattern.
 */
package org.urbancode.ucadf.core.action.ucadf.general

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction

// Get the next version sequence number.
// versionPattern is of the form: *, 1.*, 1.0.*, etc.
// versions is a list of versions used to determine the last used sequence matching the pattern.
class UcAdfGetNextVersionNumber extends UcAdfAction {
	// Action properties.
	/** The version pattern, i.e. 1.0.*, 1.0.0.*. */
	String versionPattern
	
	/** The list of versions to use to determine the next available version. */
	List<String> versions = []

	/**
	 * Runs the action.	
	 * @return The next version number (as a string).
	 */
	@Override
	public String run() {
		// Validate the action properties.
		validatePropsExist()
	
		// Strip off the trailing wild card.
		String versionPrefix = (versionPattern -~ /\*$/)
		String versionMatchRegex = (versionPrefix.replaceAll(/\./, /\\\./)) + /\d+$/

		// Find the highest sequence number matching the pattern or 0 if no matches found.		
		Integer highestSequence = -1
		for (version in versions) {
			if (version ==~ versionMatchRegex) {
				String sequenceStr = (version =~ /(\d+)$/)[0][1]
				Integer sequence = Integer.valueOf(sequenceStr)
				if (sequence > highestSequence) {
					highestSequence = sequence
				}
			}
		}
		
		String nextVersionNumber = versionPrefix + (highestSequence + 1).toString()
		
		return nextVersionNumber
	}
}
