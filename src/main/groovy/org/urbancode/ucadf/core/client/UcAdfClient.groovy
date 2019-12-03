/**
 * This is the UCADF client.
 */
package org.urbancode.ucadf.core.client

import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.CommandLineParser
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.Option
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.urbancode.ucadf.core.actionsrunner.UcAdfActionPropertyEnum
import org.urbancode.ucadf.core.actionsrunner.UcAdfActionPropertyFile
import org.urbancode.ucadf.core.actionsrunner.UcAdfActionsRunner

public class UcAdfClient {
	static Logger log
	static String webUrl = ""
	static String fileName = ""
	static Properties propertyValues = new Properties()
	static List<UcAdfActionPropertyFile> propertyFiles = []
	static List<String> propertyFileNames = []
	static Boolean debug = false

	// Set to true to run with hard-coded test file options.
	static Boolean noOptsTest = false

	public static void main(String[] args) throws Exception {
		Integer exitCode = 0
		try {
			// Initialize the logger.
			log = LoggerFactory.getLogger(UcAdfClient.class)
	
			// Parse the options.
			parseOptions(args)
	
			// Process the actions.
			runActionsfile()
		} catch (Exception e) {
			e.printStackTrace()
			exitCode = 1
		}

		println "DONE exitCode=$exitCode."		
		System.exit(exitCode)
	}

	// Parse the options.
	private static void parseOptions(String[] args) throws ParseException {
		// Used for testing.
		if (noOptsTest) {
			fileName = "src/test/resources/test.yaml"
			propertyFiles = [ fileName: "src/test/resources/test.properties" ]
		} else {
		    Option option_d = Option.builder("d")
				.desc("Debug.")
				.required(false)
				.longOpt("debug")
			    .build()
			    
		    Option option_f = Option.builder("f")
		    	.numberOfArgs(1)
				.required(false)
				.desc("Actions file name.")
			    .build()
		    
			// Properties files can be specified as: -p file1,file2 or -p file1 -p file2 -p file3,file4
			Option option_p = Option.builder("p")
				.hasArgs()
				.required(false)
				.valueSeparator((char)',')
				.desc("Properties file name.")
			    .build()

			Option option_D = Option.builder("D")
				.argName("property=value" )
				.hasArgs()
				.valueSeparator()
				.numberOfArgs(2)
				.desc("Use value for given properties." )
				.build()
	   
			Options options = new Options()
			options.addOption(option_d)
			options.addOption(option_f)
			options.addOption(option_p)
			options.addOption(option_D)
			
			CommandLineParser parser = new DefaultParser()
			CommandLine cmd = parser.parse(options, args)
			
			fileName = cmd.getOptionValue("f")
			
			if (!fileName) {
				println "ERROR: Filename is required. Use the -f option."
				System.exit(1)
			}

			if (cmd.hasOption("p")) {
				propertyFileNames = cmd.getOptionValues("p")
			}
			
			if(cmd.hasOption("D")) {
				propertyValues = cmd.getOptionProperties("D")
			}
			
			if (cmd.hasOption("d")) {
				propertyValues.put(UcAdfActionPropertyEnum.ACTIONDEBUG.getPropertyName(), true)
			}

		}

		println "=".multiply(10) + " UcAdfClient " + "=".multiply(10)
		
		log.info("Actions file name [$fileName].")
		
		for (propertyFileName in propertyFileNames) {
			propertyFiles.add([ fileName: propertyFileName ])
		}
		
		log.info("Properties files [${propertyFileNames.join(",")}].")
	}
	
	// Run the actions file.
	private static void runActionsfile() throws Exception {
		// Initialize an actions runner that uses the core actions.
		// UCD session needs no connection information because it's only going to run an action that runs an actions file.
		UcAdfActionsRunner actionsRunner = new UcAdfActionsRunner()

		actionsRunner.setCommandLinePropertyValues(propertyValues)

		// Run the actions from a file.
		actionsRunner.runAction(
			[
				action: "UcAdfRunActionsFromFile",
				fileName: fileName,
				propertyFiles: propertyFiles,
				propertyValues: propertyValues
			]
		)
	}
}
