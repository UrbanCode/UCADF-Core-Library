/**
 * This action gets a snapshot's statuses.
 */
package org.urbancode.ucadf.core.action.ucd.snapshot

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException

import groovy.util.logging.Slf4j

@Slf4j
class UcdSnapshotHasStatuses extends UcAdfAction {
	// Action properties.
	/** The application name or ID. */
	String application
	
	/** The snapshot name or ID. */
	String snapshot

	List<String> statuses

	Boolean failIfMissingStatus = false
			
	/**
	 * Runs the action.	
	 * @return True if snapshot has all of the required statuses.
	 */
	@Override
	public Boolean run() {
		// Validate the action properties.
		validatePropsExist()

		Boolean hasStatuses = true
		
		List<String> currentStatuses = actionsRunner.runAction([
			action: UcdGetSnapshotStatuses.getSimpleName(),
			actionInfo: false,
			application: application,
			snapshot: snapshot,
			returnAs: UcdGetSnapshotStatuses.ReturnAsEnum.NAMES
		])

		for (status in statuses) {
			if (!currentStatuses.contains(status)) {
				log.info "Snapshot does not have status [$status]."
				hasStatuses = false
			}
		}
		
		if (failIfMissingStatus && !hasStatuses) {
			throw new UcdInvalidValueException("Snapshot is missing status(es).")
		}
		
		return hasStatuses
	}
}
