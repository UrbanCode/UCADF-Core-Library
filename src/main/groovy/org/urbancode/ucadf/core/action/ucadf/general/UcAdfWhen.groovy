/**
 * This action is used to conditionally run a set of actions when a given condition is true. Each {@link UcAdfAction} also has the ability to have a when condition.
 */
package org.urbancode.ucadf.core.action.ucadf.general

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.actionsrunner.UcAdfActions
import org.urbancode.ucadf.core.actionsrunner.UcAdfActionsRunner
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.yaml.snakeyaml.TypeDescription
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.Constructor

import groovy.json.JsonSlurper

class UcAdfWhen extends UcAdfAction {
	// Action properties.
	/** The list of actions to run. */
	List<HashMap> actions
	
	/**
	 * Runs the action.	
	 */
	@Override
	public Object run() {
		// Run the child actions.
		for (action in actions) {
			actionsRunner.runAction(action)
		}
	}
}
