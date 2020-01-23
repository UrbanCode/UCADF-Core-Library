/**
 * This action is used to run a set of actions from a file.
 */
package org.urbancode.ucadf.core.action.ucadf.general

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.actionsrunner.UcAdfActionPackage
import org.urbancode.ucadf.core.actionsrunner.UcAdfActionPropertyEnum
import org.urbancode.ucadf.core.actionsrunner.UcAdfActionPropertyFile
import org.urbancode.ucadf.core.actionsrunner.UcAdfActions
import org.urbancode.ucadf.core.actionsrunner.UcAdfActionsFileTypeEnum
import org.urbancode.ucadf.core.actionsrunner.UcAdfActionsRunner
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.yaml.snakeyaml.TypeDescription
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.Constructor

import groovy.json.JsonSlurper

class UcAdfRunActionsFromFile extends UcAdfAction {
	// Action properties.	
	/** The actions file name. */
	String fileName
	
	/** The type of actions file: JSON, YAML, AUTO (identifies by file extension). */
	UcAdfActionsFileTypeEnum fileType = UcAdfActionsFileTypeEnum.AUTO
	
	/** The additional property values to provide to the actions runner. */
	Map<String, Object> propertyValues = [:]
	
	/** The additional propery files to provide to the actions runner. */
	List<UcAdfActionPropertyFile> propertyFiles = []
	
	/**
	 * Runs the action.	
	 * @return The return value of the action.
	 */
	@Override
	public Object run() {
		 // Display an action banner.
		println "=".multiply(5) + "Running actions file [$fileName]."

		// Validate the action properties.	/**
		validatePropsExist()

		// If this is the first invocation of running files then initialize the actions runner.
		// Otherwise, the same actions runner is used across the nested runs.
		if (!actionsRunner) {
			actionsRunner = new UcAdfActionsRunner()
		}

		// Validate the file exists.
		if (!new File(fileName).exists()) {
			// If the files does not exist then look for it in the action package directories.
			Boolean foundFile = false
			
			// Get the list of action packages.
			Map<String, UcAdfActionPackage> actionPackages = actionsRunner.getPropertyValue(UcAdfActionPropertyEnum.ACTIONPACKAGES.getPropertyName())

			// Look through the action package directories for the acction file.
			if (actionPackages) {
				for (name in actionPackages.keySet()) {
					String findActionFileName = "${actionPackages[name].getActionsDirectoryName()}/${fileName}"
					println "Looking for action file [$findActionFileName]."
					if (new File(findActionFileName).exists()) {
						fileName = findActionFileName
						foundFile = true
						logVerbose("Found action file [$fileName].")
						break
					}
				}
			}
			
			if (!foundFile) {
				throw new UcdInvalidValueException("File [$fileName] does not exist.")
			}
		}

		File actionsFile = new File(fileName)
		
		// Determine the file type.
		UcAdfActionsFileTypeEnum derivedFileType = fileType
		if (UcAdfActionsFileTypeEnum.AUTO.equals(fileType)) {
			if (fileName.toLowerCase() ==~ /.*\.(yaml|yml)$/) {
				derivedFileType = UcAdfActionsFileTypeEnum.YAML
			} else if (fileName.toLowerCase() ==~ /.*\.(json)$/) {
				derivedFileType = UcAdfActionsFileTypeEnum.JSON
			} else {
				throw new UcdInvalidValueException("Unable to automatically determine the type of file by the file extension of file [$fileName].")
			}
		}		

		// The actions to run.
		UcAdfActions actions

		switch (derivedFileType) {
			case UcAdfActionsFileTypeEnum.YAML:
				// Initialize the YAML constructor.
				Constructor constructor = new Constructor(UcAdfActions.class)
				TypeDescription yamlDescription = new TypeDescription(UcAdfActions.class)
				yamlDescription.addPropertyParameters(UcAdfActionPropertyEnum.ACTIONS.getPropertyName(), HashMap.class)
				constructor.addTypeDescription(yamlDescription)
				Yaml yaml = new Yaml(constructor)

				// Load the YAML file.
				try {
					actions = yaml.load(new FileInputStream(actionsFile))
				} catch (Exception e) {
					throw new UcdInvalidValueException(e.getMessage())
				}
				break
				
			case UcAdfActionsFileTypeEnum.JSON:
				// Load the JSON file.
				try {
					actions = new JsonSlurper().parseText(actionsFile.text)
				} catch (Exception e) {
					throw new UcdInvalidValueException(e.getMessage())
				}
				break
				
			default:
				throw new UcdInvalidValueException("Unknown file type [$fileType].")
		}

		// At this point the actions object has the propertyValues and propertyFiles that were parsed from the actions file.
		// The propertyValues and properetyFiles specifically provided to this run files action, e.g. command line optionss,
		// take precedence over those in the file.
		
		// Add the actions runner properties from those provided to this run files action.
		if (propertyValues.size() > 0) {
			logVerbose("Setting action runner properties from the run file propertyValues.")
			actionsRunner.setPropertyValues(propertyValues)
		}

		// Add the actions runner proprerties from those provided to this run files action.
		if (propertyFiles.size() > 0) {
			logVerbose("Setting action runner properties from the run file propertyFiles.")
			actionsRunner.setPropertyValuesFromFiles(propertyFiles)
		}
		
		// Run the actions.
		Object returnObject
		try {
			actionsRunner.runActions(actions)
		} catch (Exception e) {
			println "=".multiply(3) + "> Actions file [$fileName] failed."
			throw e
		}
		
		println "=".multiply(3) + "> Actions file [$fileName] DONE."
		
		return returnObject
	}
}
