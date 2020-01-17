package org.urbancode.ucadf.core.actionsrunner

import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.regex.Matcher
import java.util.regex.Pattern

import org.urbancode.ucadf.core.action.ucadf.general.UcAdfWhen
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.system.UcdSession
import org.yaml.snakeyaml.TypeDescription
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.Constructor

import com.fasterxml.jackson.databind.ObjectMapper

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j

@Slf4j
class UcAdfActionsRunner {
	public final static String PASSWORDISAUTHTOKEN = "PasswordIsAuthToken"

	// List of actions that defer property evaluation of their child actions until they run.
	private final static List<String> DEFERRED_PROPERTY_ACTIONS = [
		"UcAdfCounterLoop",
		"UcAdfItemsLoop",
		"UcAdfPageLoop",
		"UcAdfWaitLoop",
		"UcAdfWhen"
	]
		
	// Actions runner debug flag.
	private Boolean debug = false

	// The cumulative set of property values across the life of the actions runner.	
	// Properties precedence.
	// 3-System properties.
	// 2-Properties from properties file override system properties.
	// 1-Parameters provided at run time, e.g. ucdUrl override all others.
	private Map<String, String> propertyValues = new TreeMap<String, String>()

	private Map<String, String> commandLinePropertyValues = [:]
	
	// The map of action names with their respective classes.	
	private static Map<String, JavaActionClass> javaActionClasses
	
	// The initialized UCD sessions.
	private static Map<String, Map<String, Map<String, UcdSession>>> ucdSessions = [:]

	// Pattern to match nested properties in replacement text.
	private Pattern nestedPropertiesPattern = Pattern.compile('\\$\\{u[\\?]*?:.*\\$\\{(u[\\?]*)?:(.*?)\\}.*?}')
  
	// Pattern to match properties in replacement text.
	private Pattern propertiesPattern = Pattern.compile('\\$\\{(u[\\?]?):(.*?)\\}')

	// Pattern to match a property value in replacement text.	
	private Pattern propertyValuePattern = Pattern.compile("[^\\/\"']+|\"([^\"]*)\"|'([^']*)'")
  
	// The stack of actions being run.
	Stack<String> actionsStack = []

	// The current UCD session for the actions runner.
	UcdSession ucdSession
	
	// The stack of UCD sessions for actions being run.
	Stack<UcdSession> sessionsStack = []
	
	// Constructors.
	UcAdfActionsRunner() {
		// Initialize the system properties.
		initializeSystemProperties()
		
		// Initialize the actions runner action packages property.
		setPropertyValue(
			UcAdfActionPropertyEnum.ACTIONPACKAGES.getPropertyName(),
			[:]
		)

		// Load the Java action packages found in the system class paths but only do it once even if multiple action runners are initialized.
		if (!javaActionClasses) {
			javaActionClasses = [:]
			
			findJavaActionPackages(
				System.getProperty("java.class.path"),
				System.getProperty("path.separator")
			)
		}
	}

	// Get the current system environment propertys.
	public initializeSystemProperties() {
		log.debug "Setting action runner properties from system variables."
		Map<String, String> envMap = System.getenv()
		for (final String name : envMap.keySet()) {
			setPropertyValue(name, envMap.get(name))
		}
	}	

	// Initialize the action packages property used to find action files at run time.
	public initializeActionPackagesProperty() {
		// The the packages information from the actions runner properties.
		String packagesDir = getPropertyValue(UcAdfActionPropertyEnum.UCADFPACKAGESDIR.getPropertyName())
		String packageVersions = getPropertyValue(UcAdfActionPropertyEnum.UCADFPACKAGEVERSIONS.getPropertyName())
		
		// Initialize the action packages property.		
		// Get the action package information for each package version.
		Map<String, UcAdfActionPackage> actionPackages = new LinkedHashMap()
		
		if (packagesDir && packageVersions) {		
			log.debug "Package versions [$packageVersions] in [$packagesDir]."

			for (packageVersionSpec in packageVersions.split(",")) {
				String packageName, packageVersion
				(packageName, packageVersion) = packageVersionSpec.split(":")
				if (!packageName) {
					throw new UcdInvalidValueException("Package name is blank in [$packageVersionSpec].")
				}
				if (!packageVersion) {
					throw new UcdInvalidValueException("Package version is blank in [$packageVersionSpec]")
				}
	
				String packageVersionDirectory = "$packagesDir/$packageName/$packageVersion"
	
				UcAdfActionPackage actionPackage = new UcAdfActionPackage()
				actionPackage.setName(packageName)
				actionPackage.setVersion(packageVersion)
				actionPackage.setDirectoryName(packageVersionDirectory)
				actionPackage.setActionsDirectoryName("${packageVersionDirectory}/Actions")
				
				// Save the action package information in the action package map.
				log.debug "Using action package [$packageName] [$packageVersion] in [$packageVersionDirectory]."
				
				actionPackages.put(packageName, actionPackage)
			}
		}
		
		// Set the actions runner action packages property that allows the runner to find action files in the Actions directories at run time.
		setPropertyValue(
			UcAdfActionPropertyEnum.ACTIONPACKAGES.getPropertyName(),
			actionPackages
		)
	}	
	
	// Run a single action defined as a map.
	public Object runAction(final Map actionMap) {
		return runAction(new JsonBuilder(actionMap))
	}

	// Run a single action defined as a JsonBuilder	.
	public Object runAction(final JsonBuilder jsonBuilder) {
		return runAction(jsonBuilder.toString())
	}

	// Run a single action defined as a JSON string.
	public Object runAction(final String jsonStr) {
		// Create an empty actions collection.
		UcAdfActions actions = new UcAdfActions()
		
		// Now parse one action provided as a string and add it to the actions collection.
		actions.addAction(
			new JsonSlurper().parseText(jsonStr)
		)

		return runActions(actions)
	}

	// Run a single action defined as an action object.
	public Object runAction(final UcAdfAction action) {
		return runActions([ action ])
	}

	// Run the actions.
	public Object runActions(final UcAdfActions actions) {
		// Set debug flag from property value.
		if (!debug) {
			debug = getPropertyValue(UcAdfActionPropertyEnum.ACTIONDEBUG.getPropertyName())
		}
		
		// Initialize the ACTIONPACKAGES runner property.
		initializeActionPackagesProperty()

		// Initialize the outProps runner property for the actions run.
		setPropertyValue(UcAdfActionPropertyEnum.OUTPROPS.getPropertyName(), new Properties())

		// Set the runner property values from the actions property values.
		setPropertyValues(actions.getPropertyValues())
		
		// Set the runner property values from property files.
		setPropertyValuesFromFiles(actions.getPropertyFiles())

		// Override the property values with any command line property values.
		setPropertyValues(commandLinePropertyValues)

		if (debug) {
			dump()
		}

		// The object value that will be returned.
		// This really only has value when a single action is being run or the return of the last action is of interest.
		Object returnObject = new Properties()

		// The map of action classes loaded for this runner.
		Map loadedActionClasses = [:]
	
		// The Jersey object mapper used by these actions.
		ObjectMapper actionsMapper = new ObjectMapper()

		// Pre-process each action to load the appropriate action class.
		for (actionMap in actions.getActions()) {
			String actionName = actionMap.get(UcAdfActionPropertyEnum.ACTION.getPropertyName())
			
			// If the action class has already been loaded then don't load it again.
			if (loadedActionClasses.containsKey(actionName)) {
				continue
			}

			Class actionClass = getActionClass(actionName)

			// Register the action class with the Jackson object mapper.
			actionsMapper.registerSubtypes(actionClass)
			loadedActionClasses.put(actionName, actionClass)
		}

		// Run each action in the action list.
		for (actionMap in actions.getActions()) {
			returnObject = runAction(
				actionsMapper,
				actionMap
			)
		}

		return returnObject
	}

	// Run and action.
	// The action map is provided so that property replacement can occur.
	public Object runAction(
		final ObjectMapper actionsMapper,
		final Map actionMap) {

		// The object value that will be returned.
		Object returnObject = new Properties()
	
		// Add the action to the stack.
		String actionName = actionMap.get(UcAdfActionPropertyEnum.ACTION.getPropertyName())
		actionsStack.push(actionName)

		// 	Try/finally used to ensure pop actions stack.	
		try {
			// If no value was provided for actionInfo then default it to true.
			Boolean actionInfo = actionMap.get(UcAdfActionPropertyEnum.ACTIONINFO.getPropertyName())
			if (actionInfo) {
				println "Begin [" + actionsStack.join("->") + "]."
			}
			
			// Set the runner property values from the action property values.
			setPropertyValues(actionMap.get(UcAdfActionPropertyEnum.PROPERTYVALUES.getPropertyName()))
	
			// Set the runner property values from property files.
			setPropertyValuesFromFiles(actionMap.get(UcAdfActionPropertyEnum.PROPERTYFILES.getPropertyName()))
	
			// Override the property values with any command line property values.
			setPropertyValues(commandLinePropertyValues)
			
			// If the action defers variable replacement then save the actions before variable replacement, then reset them afterwards.		
			List<LinkedHashMap> saveActions
			if (DEFERRED_PROPERTY_ACTIONS.contains(actionMap[UcAdfActionPropertyEnum.ACTION.getPropertyName()])) {
				saveActions = actionMap[UcAdfActionPropertyEnum.ACTIONS.getPropertyName()]
				actionMap[UcAdfActionPropertyEnum.ACTIONS.getPropertyName()] = []
			}
	
			// Replace the variables with properties in the action map.
			replaceVariablesInMap(actionMap)
	
			// Reset actions for deferred replacement.		
			if (DEFERRED_PROPERTY_ACTIONS.contains(actionMap[UcAdfActionPropertyEnum.ACTION.getPropertyName()])) {
				actionMap[UcAdfActionPropertyEnum.ACTIONS.getPropertyName()] = saveActions
			}
	
			// Serialize the action map into a runner action object after the property values have been replaced.
			UcAdfAction action
			try {
				// Convert action using JSON deserializer.
				action = actionsMapper.convertValue(
					actionMap,
					UcAdfAction
				)
			} catch (Exception e) {
				throw new UcdInvalidValueException(e.getMessage() + "\n" + actionMap)
			}
	
			// If there's a when property value then evaluate it to determine if the action should be run.
			// Do not evaluate a when condition of the UcAdfWhen action here but rather run the action so that it can perform the elseActions if needed.
			if (action.getWhen() && !action.getAction().equals(UcAdfWhen.getSimpleName()) && evaluateWhen(action) == false) {
				log.debug("Skipping action [$actionName}] when [${action.getWhen()}].")
			} else {
				// Show the action properties.
				action.showProperties()
	
				// Add the UCD session to the stack.
				sessionsStack.push(ucdSession)
			
				// 	Try/finally used to ensure pop sessions stack.	
				try {
					// The UCD configuration may have been provided as properties of the individual action.
					// Start with those values and determine if they need to be supplemented with property values from the runner.
					initializeUcdSession(action, actionInfo)
		
					// The action can access the UCD session.
					action.setUcdSession(ucdSession)
					
					// The action can access the actions runner.						
					action.setActionsRunner(this)
		
					debugMessage("${action.getAction()} run.")
		
					// Run the action and return the object.
					returnObject = action.run()
					
					// If the action's run method added outProps values then add those to the actions runner outProps property.
					// Keep in mind that a UcAdfSetActionProperties action may also add outProps value.
					if (action.getOutProps().size() > 0) {
						Properties outProps = getPropertyValue(UcAdfActionPropertyEnum.OUTPROPS.getPropertyName())
					
						// Add the action outProps the action runner outProps.
						action.getOutProps().each { k, v ->
							outProps.put(k, v)
						}
			
						// Set the outProps property with the current outProps values.
						setPropertyValue(UcAdfActionPropertyEnum.OUTPROPS.getPropertyName(), outProps)
					}
					
					debugMessage("action ${action.getAction()} return ${returnObject}.")
				} finally {
					// Remove the session from the stack.
					ucdSession = sessionsStack.pop()
				}
			}
	
			// Set the return object in the actionReturn property.
			setPropertyValue(
				UcAdfActionPropertyEnum.ACTIONRETURN.getPropertyName(),
				returnObject
			)
				
			// Optionally, set the return object in the specified property.
			if (action.getActionReturnPropertyName()) {
				setPropertyValue(
					action.getActionReturnPropertyName(), 
					returnObject
				)
			}	
	
			// Action end processing.
			action.end()
		} finally {
			// Remove the action from the stack.
			actionsStack.pop()
		}

		return returnObject
	}

	// Evaluate a when.
	public Object evaluateWhen(UcAdfAction action) {
		Binding binding = new Binding()
		binding.setVariable("action", action)

		// Run the when Groovy script to analyze the when condition.		
		Object returnObject = runGroovyScript(
			binding,
			action.getWhen()
		)

		Boolean returnEvaluate
		
		if (returnObject == false || "false".equals(returnObject) || "".equals(returnObject)) {
			returnEvaluate = false
		} else {
			returnEvaluate = true
		}
		
		debugMessage("evaluateWhen returnObject=[$returnObject] (${returnObject.getClass().getSimpleName()})")
		
		return returnEvaluate
	}
	
	// Gets a UCD session using the provided connection information and/or the current actions runner property values.
	public initializeUcdSession(
		final UcAdfAction action,
		final Boolean actionInfo = true) {
		
		// Values from the action properties.
		String ucdUrl = action.getUcdUrl()
		String ucdUserId = action.getUcdUserId()
		String ucdUserPw = action.getUcdUserPw()
		String ucdAuthToken = action.getUcdAuthToken()

		debugMessage("Initializing UCD session ucdUrl=[$ucdUrl] ucdUserId=[$ucdUserId] ucdSession=[$ucdSession].")
		
		UcdSession saveUcdSession = ucdSession
		
		// Determine if a new session is needed.
		Boolean needNewSession = true
		if (ucdSession) {
			if (ucdSession.getUcdUrl() == ucdUrl && (ucdSession.getUcdUserId() == ucdUserId && ucdSession.getUcdUserPw() == ucdUserPw) || (PASSWORDISAUTHTOKEN.equals(ucdUserId) && ucdSession.getUcdUserPw() == ucdAuthToken)) {
				// There's currently a session and the action properties match it.
				debugMessage("Using existing matching session [$ucdSession].")
				needNewSession = false
			} else {
				// There's currently a session and no action properties to override it.
				if (!ucdUrl && !ucdUserId && !ucdUserPw && !ucdAuthToken) {
					debugMessage("Using existing session [$ucdSession].")
					needNewSession = false
				}
			}
		}

		if (needNewSession) {
			// Set the UCD connection URL.
			if (!ucdUrl) {
				// Use action property value if set.
				ucdUrl = getPropertyValue(UcdSession.PROPUCDURL)
				
				// If no action property value then use the AH_WEB_URL system enviornment variable.
				if (!ucdUrl) {
					ucdUrl = getPropertyValue(UcdSession.PROPUCDAHWEBURL)
				}
			}
			
			// If the action didn't specify connection information then use the values from the runner properties.
			if (!ucdUserId && !ucdUserPw && !ucdAuthToken) {
				ucdUserId = getPropertyValue(UcdSession.PROPUCDUSERID)
				ucdUserPw = getPropertyValue(UcdSession.PROPUCDUSERPW).toString()
				ucdAuthToken = getPropertyValue(UcdSession.PROPUCDAUTHTOKEN).toString()
			}

			// Determine if the user ID/password or an auth token should be used for authentication.
			// If no user ID was provided then set the user ID for token authentication.
			if (!ucdUserId) {
				ucdUserId = PASSWORDISAUTHTOKEN
				debugMessage("No ucdUserId value so setting it to PasswordIsAuthToken.")
			}
			
			// If the user ID is a token user and no password was provided then use the auth token as the user password.
			if (PASSWORDISAUTHTOKEN.equals(ucdUserId) && !ucdUserPw.toString()) {
				debugMessage("User is PasswordIsAuthToken and no password provided so setting ucdUserPw to ucdAuthToken.")
				
				// If no auth token value then use DS_AUTH_TOKEN system environment variable.
				if (!ucdAuthToken) {
					ucdAuthToken = getPropertyValue(UcdSession.PROPUCDDSAUTHTOKEN)
				}
				
				ucdUserPw = ucdAuthToken
			}
	
			// If the UCD connection has never been initialized then save the properties of the first initializtion to use going forward.
			if (!ucdUrl) {
				setPropertyValue(UcdSession.PROPUCDURL, ucdUrl)
			}
			
			if (!ucdUserId) {
				setPropertyValue(UcdSession.PROPUCDUSERID, ucdUserId)
				setPropertyValue(UcdSession.PROPUCDUSERPW, ucdUserPw)
				setPropertyValue(UcdSession.PROPUCDAUTHTOKEN, ucdAuthToken)
			}
	
			// If the specific session has already been created then reuse it, otherwise create a new session.		
			if (!ucdSessions.containsKey(ucdUrl)) {
				ucdSessions[ucdUrl] = [:]
			}
			if (!ucdSessions[ucdUrl].containsKey(ucdUserId)) {
				ucdSessions[ucdUrl][ucdUserId] = [:]
			}

			if (ucdSessions[ucdUrl][ucdUserId].containsKey(ucdUserPw)) {
				// Get the existing session based on the derived connection values.
				ucdSession = ucdSessions[ucdUrl][ucdUserId][ucdUserPw]
				debugMessage("Using session found for ucdUrl=[$ucdUrl] ucdUserId=[$ucdUserId] ucdSession=[$ucdSession].")
			} else {
				// Initialize the UCD session based on the derived connection values.
				if (ucdUrl && ucdUserId && ucdUserPw) {
					ucdSession = new UcdSession(
						ucdUrl,
						ucdUserId,
						ucdUserPw
					)
					
					ucdSessions[ucdUrl][ucdUserId][ucdUserPw] = ucdSession
					
					debugMessage("New session ucdUrl=[$ucdUrl] ucdUserId=[$ucdUserId] ucdSession=[$ucdSession].")
				}
			}
		}
		
		if (actionInfo && ucdSession && (saveUcdSession != ucdSession)) {
			log.info "Actions runner current session ucdUrl=[${ucdSession.getUcdUrl()}] ucdUser=[${ucdSession.getUcdUserId()}]."
		}
	}	
	
	// Output a debug message.
	public debugMessage(final String message) {
		if (debug) {
			println "DEBUG: ${message}"
		}
	}    

	// Set the runner property values from property files.
	public setPropertyValuesFromFiles(final List<UcAdfActionPropertyFile> propertyFileDefs) {
		// Set the runner property values from property files.
		for (UcAdfActionPropertyFile propertyFileDef in propertyFileDefs) {
			String propertyFileName = propertyFileDef.getFileName()
			
			// Replace variables in the property file name.
			propertyFileName = replaceVariablesInText(propertyFileName)

			File propertiesFile = new File(propertyFileName)
			
			// Determine the file type.
			UcAdfActionsFileTypeEnum derivedFileType = propertyFileDef.getFileType()
			if (UcAdfActionsFileTypeEnum.AUTO.equals(derivedFileType)) {
				if (propertyFileName.toLowerCase() ==~ /.*\.(yaml|yml)$/) {
					derivedFileType = UcAdfActionsFileTypeEnum.YAML
				} else if (propertyFileName.toLowerCase() ==~ /.*\.(properties)$/) {
					derivedFileType = UcAdfActionsFileTypeEnum.PROPERTIES
				} else {
					throw new UcdInvalidValueException("Unable to automatically determine the type of file by the file extension of file [$propertyFileName].")
				}
			}		

			if (!propertiesFile.exists()) {
				Boolean failNotFound = propertyFileDef.getFailNotFound()
				if (failNotFound) {
					throw new UcdInvalidValueException("Property file [$propertyFileName] not found.")
				} else {
					log.info "Property file [$propertyFileName] not found. failNotFound=${failNotFound}."
					continue
				}
			}
			
			log.info "Setting properties from file [${propertyFileName}]."
			Properties props = new Properties()
			try {
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
							// Parse the properties file.
							UcAdfActions actions = yaml.load(new FileInputStream(propertiesFile))
							
							// Set the runner property values from the actions property values.
							setPropertyValues(actions.getPropertyValues())
						} catch (Exception e) {
							throw new UcdInvalidValueException(e.getMessage())
						}
						break
						
					case UcAdfActionsFileTypeEnum.PROPERTIES:
						// Load the PROPERTIES file.
						try {
							FileInputStream propsInputStream = new FileInputStream(propertiesFile)
							props.load(propsInputStream)
							propsInputStream.close()
			
							// Add the properties values to the propertys map.
							props.each { k, v ->
							  setPropertyValue(k, v)
							}
						} catch (Exception e) {
							throw new UcdInvalidValueException(e.getMessage())
						}
						break
						
					default:
						throw new UcdInvalidValueException("Unknown file type [$derivedFileType].")
				}
			} catch (Exception e) {
				throw new UcdInvalidValueException(e.getMessage())
			}
		}
	}
	
	// Set a property.
	public setPropertyValue(
		final String propertyName, 
		final Object propertyValue) {

		// Set debugging.
		if (UcAdfActionPropertyEnum.ACTIONDEBUG.getPropertyName().equals(propertyName)) {
			debug = Boolean.valueOf(propertyValue)
		}

		// If the existing property value is a Map and the property value being provided is a Map then merge them.
		if (propertyValue instanceof Map && propertyValues.containsKey(propertyName) && propertyValues.get(propertyName) instanceof Map) {
			debugMessage("Setting property name [$propertyName] value [$propertyValue] (Map).")
			
			// Merge the maps.
			propertyValues.put(propertyName, propertyValues.get(propertyName) + propertyValue)
		} else {
			debugMessage("Setting property name [$propertyName] value [$propertyValue].")

			// Set the property value.				
			propertyValues.put(propertyName, propertyValue)
		}
	}

	// Set property values from a map collection.
	public setPropertyValues(final Map<String, Object> propertyValues) {
		propertyValues.each { k, v ->
			setPropertyValue(k, v)
		}
	}	
	
	// Set a command line property.
	public setCommandLinePropertyValue(
		final String propertyName, 
		final Object propertyValue) {

		debugMessage("Setting command line property name [$propertyName] value [$propertyValue].")
			
		commandLinePropertyValues.put(propertyName, propertyValue)
	}

	// Set command line property values from a map collection.
	public setCommandLinePropertyValues(final Properties commandLinePropertyValues) {
		commandLinePropertyValues.each { k, v ->
			setCommandLinePropertyValue(k, v)
		}
	}	
	
	// Get a property. If not defined then return an empty string.
	// Syntax: ${u:FOO}, ${u:FOO/BAR}, ${p?:FOO/BAR/1}, etc.
	public Object getPropertyValue(
		final String propertyName,
		final String replaceType = 'u?') {

		Object returnObject = propertyValues

		// Each property name is delimited by a / unless enclosed in single or double quotes.		
		List<String> keys = getSplitPropertyName(propertyName)
		
		for (key in keys) {
			if (!key) {
				continue
			}

			if (returnObject instanceof List) {
				if (key.equals("#")) {
					returnObject = returnObject.size()
				} else if (key.isNumber()) {
					returnObject = (returnObject as List)[(key as Integer)]
				} else {
					throw new UcdInvalidValueException("List index value of [$key] is not an index number.")
				}
			} else {
				if ((returnObject instanceof Map && returnObject.containsKey(key)) || returnObject.hasProperty(key)) {
					returnObject = returnObject[key]
				} else {
					if ('u'.equals(replaceType)) {
						throw new UcdInvalidValueException('Property ${u:' + propertyName + '} not defined.')
					} else {
						returnObject = ""
						break
					}
				}
			}
		}

		debugMessage("Getting property name [$propertyName] value [$returnObject] ${returnObject.getClass()}.")
		
		return ((returnObject != null) ? returnObject : "")
	}

	// Split a property name into segments.
	public List<String> getSplitPropertyName(final String propertyName) {
		// Each property name is delimited by a / unless enclosed in single or double quotes.
		List<String> keys = new ArrayList<String>()
		Matcher regexMatcher = propertyValuePattern.matcher(propertyName)
		while (regexMatcher.find()) {
			if (regexMatcher.group(1) != null) {
				// Add double-quoted string without the quotes
				keys.add(regexMatcher.group(1))
			} else if (regexMatcher.group(2) != null) {
				// Add single-quoted string without the quotes
				keys.add(regexMatcher.group(2))
			} else {
				// Add unquoted word
				keys.add(regexMatcher.group())
			}
		}
		
		return keys
	}

	// Determine if a property value exists.
	public Boolean propertyValueExists(final String propertyName) {
		return propertyValues.containsKey(propertyName)
	}	
	
	// Recurse the map and replace variables with property values.	
	public Object replaceVariablesInMap(Object object, Integer indent = 0) {
		//println "\t".multiply(indent) + "object=$object class=${object.getClass()}"
		indent++
		
		if (object instanceof Map) {
			// Set map values with replaced variables.
			object.each { key, childObj ->
				// If the key has a property name in it then it needs to be replaced.
				String replacedKey = replaceVariablesInMap(key, indent)
				if (key.equals(replacedKey)) {
					object.put(key, replaceVariablesInMap(childObj, indent))
				} else {
					object.remove(key)
					object.put(replacedKey, replaceVariablesInMap(childObj, indent))
				}
			}
		} else if (object instanceof List) {
			// Process each list item.
			for (Integer i = 0; i < object.size(); i++) {
				Object childObj = object[i]
				object[i] = replaceVariablesInMap(childObj, indent)
			}
		} else {
			// Do string variable replacement.
			if (object instanceof String) {
				object = replaceVariablesInText(object)
			}
		}
		
		//println "\t".multiply(indent) + "returning object=$object class=${object.getClass()}"

		return object
	}

	// Replace environment properties $(x) or ${x}with actual values.
    public Object replaceVariablesInText(final String text) {
    	debugMessage("Properties replace text before [$text].")

		Object returnObject
		
		if (text ==~ /^\s*Eval\(.*?\)\s*$/) {
			returnObject = processEvalReplace(text)
		} else {
			// Initialize the text to be replaced.
			String returnText = text
			
			// Replace nested property values first.
			Boolean replaceNested = true
			while (replaceNested) {
				Matcher nestedPropertiesMatches = nestedPropertiesPattern.matcher(returnText)
	
				replaceNested = false
				while (nestedPropertiesMatches.find()) {
					// First group in match is u or u?, second group in match is the property name.
					String uValue = nestedPropertiesMatches.group(1)
					String propName = nestedPropertiesMatches.group(2)
	
					// Get the property value for the nested property.				
					String propertyValueText = replaceVariablesInText('${' + uValue + ':' + propName + '}')
	
					// Replace the nested property variable with the property value.				
					String regex = '\\$\\{' + (uValue == 'u?' ? 'u\\?:' : 'u:') + propName + '\\}'
					returnText = returnText.replaceAll(regex, propertyValueText)
					
					// Indicate to keep processing.
					replaceNested = true
				}
			}
	
			// Replace the property values.
	    	Boolean isTextReturn = true
	    	Matcher matcher = propertiesPattern.matcher(returnText)
	    	while (matcher.find()) {
	    		// First group in match is u or u?, second group in match is the property name.
	    		Object propertyValue = getPropertyValue(
					matcher.group(2),
					matcher.group(1)
				)
				
				// If the property value can be converted into a String object then do text replacement.
				if (propertyValue instanceof String || propertyValue instanceof Number || propertyValue instanceof Boolean) {
					String propertyValueText = propertyValue
	
					// Recursively replace property values.			
					if (propertiesPattern.matcher(propertyValueText).find()) {
						propertyValueText = replaceVariablesInText(propertyValueText)
					}
					
					// If the property value is not blank then replace backslash with four backslashes so that replaceall works correctly.
		    	    if (propertyValueText) {
						// Needed for the upcoming replaceAll so that it doesn't lose the backslashes.
						propertyValueText = propertyValueText.replace("\\", "\\\\")
		    	    }
	
					// Skip replacing a property value that has syntax like a variable name, e.g. ${p:foo}
					if (propertyValueText ==~ /\$\{.*\}/) {
						returnText = propertyValueText
					} else {
						Pattern subexpr = Pattern.compile(Pattern.quote(matcher.group(0)))
						returnText = subexpr.matcher(returnText).replaceAll(Matcher.quoteReplacement(propertyValueText))
					}
				} else {
					// If the property value is a complex type then return it as-is.
					// This only works for a single property replacement in a given string.
					isTextReturn = false
					returnObject = propertyValue
					break
				}
	    	}
			
			if (isTextReturn) {
				debugMessage("Properties replace text after [$returnText].")
				returnObject = returnText
			}
		}
		
    	return returnObject
    }
	
	// Process an Eval replacement.
	public Object processEvalReplace(final String text) {
		Object returnObject
		
		// Get the string inside of Eval(.*).
		String evalStr = text.replaceAll(/^\s*Eval\((.*?)\)\s*$/, '$1')
		Object evalParm = replaceVariablesInText(evalStr)
		
		debugMessage("Begin Eval [$evalParm] ${evalParm.getClass().getSimpleName()}")
		
		if (!(evalParm instanceof String)) {
			throw new UcdInvalidValueException("The Eval parameter [$evalParm] must be of type String not [${evalParm.getClass().getSimpleName()}")
		}

		returnObject = Eval.me(evalParm)
		
		debugMessage("End Eval [$returnObject] [${returnObject.getClass().getSimpleName()}].")

		return returnObject
	}

	// Run Groovy script in Groovy shell.
	public Object runGroovyScript(
		final Binding binding, 
		final String scriptText) {
		
		Object returnObject
		
		// Prepare to bind the variables to the script.
		GroovyShell shell = new GroovyShell(binding)
		
		// Execute the script.
		returnObject = shell.evaluate(scriptText)
		
		return returnObject
	}
	
	// Split a comma-delimited string.	
	public List<String> splitCommaDelimited(final String text) {
		List<String> splitStrings = new ArrayList<String>()
		
		Integer iStart = 0
		Boolean insideDoubleQuotes = false
		Boolean insideSingleQuotes = false
		for (Integer iCurrent = 0; iCurrent < text.length(); iCurrent++) {
			if (text.charAt(iCurrent) == "'") {
				insideSingleQuotes = !insideSingleQuotes
			} else if (text.charAt(iCurrent) == '\"') {
				insideDoubleQuotes = !insideDoubleQuotes
			}

			Boolean atLastCharacter = (iCurrent == text.length() - 1)

			if (atLastCharacter) {
				splitStrings.add(text.substring(iStart).trim())
			} else if (text.charAt(iCurrent) == ',' && !insideSingleQuotes && !insideDoubleQuotes) {
				splitStrings.add(text.substring(iStart, iCurrent).trim())
				iStart = iCurrent + 1
			}
		}

		return splitStrings
	}
	
	// Get the action class for a given action name.	
	public Class getActionClass(final String actionName) {
		Class actionClass
		
		JavaActionClass javaActionClass = javaActionClasses.get(actionName)
		if (javaActionClass) {
			// If the action class has already been loaded then use it.
			actionClass = javaActionClass.getActionClass()

			if (!actionClass) {			
				// Load the action class.
				String actionClassName = javaActionClass.getActionClassName()
				try {
					actionClass = this.getClass().getClassLoader().loadClass(actionClassName)
				} catch (ClassNotFoundException e) {
					// Ignore class not found exceptions as attempting to load class from each package.
				}
			}
		}
		
		// If wasn't able to load the action class from any package then the action name wasn't found.
		if (!actionClass) {
			throw new UcdInvalidValueException("Unable to find action class for action [$actionName] in available actions.")
		}
		
		return actionClass
	}

	// Find the action class from the classes in the Java classpath.
    private findJavaActionPackages(
		final String classpath,
		final String pathSeparator) {
		
		log.debug "findJavaActionPackages classpath=[$classpath] pathSeparator=[$pathSeparator]."
		
        List<String> classPaths = classpath.split(pathSeparator)
		
		log.debug "findJavaActionPackages classPaths [$classPaths]."

		// Process each classpath to find action packages to load.
        for (String path in classPaths) {
			log.debug "Processing class path [$path]."
            File file = new File(path)
            if (file.exists()) {
                findJavaActionPackages(
					file, 
					file
				)
            }
        }
    }

	// Recursive method to load action packages.
    private String findJavaActionPackages(
		final File root, 
		final File file) {

		if (file.isDirectory()) {
			// Process each directory entry.
	        for (File child : file.listFiles()) {
				// Skip certain names that we know aren't usable.
				if (child.getName() ==~ /.*META-INF.*/) {
					continue
				}
				
				// Recursive process directories.
				findJavaActionPackages(
					root,
					child
				)
			}
		} else {
			// If the file is a jar file then get the classes in it.
            if (file.getName().toLowerCase().endsWith(".jar")) {
				log.debug "Processing jar [$file]."
				
                JarFile jarFile
				
                try {
                    jarFile = new JarFile(file)
                } catch (Exception ex) {
					// Ignore missing jar file.
                }
				
                if (jarFile) {
                    Enumeration<JarEntry> entries = jarFile.entries()
                    while (entries.hasMoreElements()) {
                        JarEntry entry = entries.nextElement()

						if (entry.getName() ==~ /org\/urbancode\/ucadf\/.*?\/action\/.*\.class$/) {
							String actionClassName = entry.getName().replaceAll(/\\//, ".").replaceAll(/^(org\.urbancode\.ucadf\..*?.action\..*?)\.class/, '$1')
							String actionName = actionClassName.substring(actionClassName.lastIndexOf('.') + 1)
							if (actionName && !(actionName ==~ /.*\$.*/)) {
								log.debug "actionName=[$actionName] actionClassName=[$actionClassName] from jar file."
								
								javaActionClasses.put(
									actionName, 
									new JavaActionClass(actionClassName)
								)
							}
                    	}
                    }
                }
			} else {
				// Determine if the file name is a UCADF action class.
				String dottedPath = file.getPath().replaceAll(Matcher.quoteReplacement(File.separator), ".")
				if (dottedPath ==~ /.*?\.org\.urbancode\.ucadf\..*?\.action\..*?\.class$/) {
					String actionClassName = dottedPath.replaceAll(/.*?\.(org\.urbancode\.ucadf\..*?.action\..*?)\.class/, '$1')
					String actionName = actionClassName.substring(actionClassName.lastIndexOf('.') + 1)
					if (actionName && !(actionName ==~ /.*\$.*/)) {
						log.debug "actionName=[$actionName] actionClassName=[$actionClassName] from class file."
						
						javaActionClasses.put(
							actionName, 
							new JavaActionClass(actionClassName)
						)
					}
				}
			}
		}
    }
	
	// Dump the properties.
	public dump() {
		println "=========================================================================="
		println "Dump of Actions Runner Properties (excluding system environment variables)"
		println "=========================================================================="
		Map<String, String> envMap = System.getenv()
		
		for (Map.Entry<String, String> entry : propertyValues.entrySet()) {
			if (!envMap.containsKey(entry.getKey())) {
				System.out.println(entry.getKey() + "=" + entry.getValue())
			}
		}
		println "---"
	}

	// This class is used to keep information about an action class.
	// When the actions runner starts it will create a map of all of the action names and have one of these objects associated with the map.
	// The object will initially only contain the action class name but as actions are run their action classes will be dynamically loaded and the action class saved.	
	private class JavaActionClass {
		String actionClassName
		Class actionClass
		
		JavaActionClass(
			final String actionClassName,
			final Class actionClass = null) {
			
			this.actionClassName = actionClassName
			this.actionClass = actionClass
		}
	}
}
