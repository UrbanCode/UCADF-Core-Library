// This is executed by the UcAdfCorePluginActionsRunner script.
package org.urbancode.ucadf.core.actionsrunner.plugin

import org.urbancode.ucadf.core.actionsrunner.UcAdfActionPropertyEnum
import org.urbancode.ucadf.core.actionsrunner.UcAdfActions
import org.urbancode.ucadf.core.actionsrunner.UcAdfActionsRunner
import org.urbancode.ucadf.core.model.ucd.applicationProcessRequest.UcdApplicationProcessRequestOutPropEnum
import org.urbancode.ucadf.core.model.ucd.applicationProcessRequest.UcdApplicationProcessRequestStatusEnum
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.yaml.snakeyaml.TypeDescription
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.Constructor

import groovy.util.logging.Slf4j

@Slf4j
public class UcAdfPluginActionsRunner {
	// Input properties for running UCADF actions.
	final static String INPROP_ACTIONSTEXT = "actionsText"

	// Main.
	public static void main(String[] args) throws Exception {
		try {
			// Get the information about the UCD plugin input/output properties files.
			if ((args as String[]).length != 4) {
				throw new UcdInvalidValueException("Must provide exactly four arguments: A UCADF packages directory name, package versions specification, input properties file name, and output properties file name.")
			}

			// The location of the downloaded packages.			
			final String packagesDir = args[0]
			
			// The package versions specification in the format packageName:packageVersion,[...]
			final String packageVersions = args[1]
			
			// The input properties file name.
			final String inPropsFileName = args[2]
			
			// The output properties file name.
			final String outPropsFileName = args[3]

			// Initialize the actions runner.
			UcAdfActionsRunner actionsRunner = new UcAdfActionsRunner()

			// Set the packages directory property that will be used to initialize the ACTIONPACKAGES property.		
			actionsRunner.setPropertyValue(
				UcAdfActionPropertyEnum.UCADFPACKAGESDIR.getPropertyName(),
				packagesDir
			)

			// Set the package versions property that will be used to initialize the ACTIONPACKAGES property.		
			actionsRunner.setPropertyValue(
				UcAdfActionPropertyEnum.UCADFPACKAGEVERSIONS.getPropertyName(),
				packageVersions
			)

			// Initialie the ACTIONPACKAGES runner property.
			actionsRunner.initializeActionPackagesProperty()
	
			// Initialize the UCADF plugin tool.
			final UcAdfPluginTool ucAdfPluginTool = new UcAdfPluginTool()
			
			// Get the input properties.
			final Properties inProps = ucAdfPluginTool.getStepProperties(inPropsFileName)
			
			// Get the plugin input property values.
			final String actionsText = inProps[INPROP_ACTIONSTEXT]
		
			// Validate enough information was provided to execute.		
			if (!actionsText) {
				throw new UcdInvalidValueException("No actionsText provided.")
			}

			// The output properties.
			Properties outProps = new Properties()
			
			if (actionsText) {
				// Replace leading tabs with spaces and CRLF with LF.
				String derivedActionsText = actionsText.replaceAll(/(?m)\t/, '    ').replaceAll(/(?m)\r\n/, "\n")

				log.info "Running actions from:\n[$derivedActionsText]."

				// Initialize the YAML constructor.
				Constructor constructor = new Constructor(UcAdfActions.class)
				TypeDescription yamlDescription = new TypeDescription(UcAdfActions.class)
				yamlDescription.addPropertyParameters(UcAdfActionPropertyEnum.ACTIONS.getPropertyName(), HashMap.class)
				constructor.addTypeDescription(yamlDescription)
				Yaml yaml = new Yaml(constructor)

				try {
					// Parse the YAML text.
					UcAdfActions actions = yaml.load(derivedActionsText)
					
					// Run the actions.
					actionsRunner.runActions(actions)
					
					// Get the output properties from the action.
					outProps = actionsRunner.getOutProps()
				} catch (Exception e) {
					if (e instanceof UcdInvalidValueException) {
						// Throw with exception message and no stack trace.
						throw new UcdInvalidValueException(e.getMessage())
					} else {
						// Rethrow an unknown exception to get a stack trace.
						throw(e)
					}
				}
			}
			
			// Write the output properties file.
			ucAdfPluginTool.writeOutputProperties(
				outProps,
				outPropsFileName
			)
			
			// If the plugin status property is set to Failure then terminate with non-zero error code.
			String pluginStatus = outProps.get(UcdApplicationProcessRequestOutPropEnum.PROCESSSTATUS.getPropertyName())
			if (UcdApplicationProcessRequestStatusEnum.FAILURE.getValue() == pluginStatus) {
				log.error "Plugin status [$pluginStatus]."
				System.exit(1)
			}
		} catch (Exception e) {
			String errorMsg = "\n" + '='.multiply(20) + " ERROR " + '='.multiply(20) + "\n" + e.getMessage() + "\n" + '='.multiply(47) + "\n\n" + e.getStackTrace()
			log.error errorMsg
			
			// Not writing the error message to the output properties file because you can get hangs if the UC agent can't interpret what's in the file.
			System.exit(1)
		}

		println "DONE"
	}
}
