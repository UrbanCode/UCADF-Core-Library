package org.urbancode.ucadf.core.actionsrunner

import org.urbancode.ucadf.core.model.ucadf.UcAdfSecureString
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucadf.objectmapper.UcAdfObjectMapper
import org.urbancode.ucadf.core.model.ucd.general.UcdObject
import org.urbancode.ucadf.core.model.ucd.system.UcdSession

import com.fasterxml.jackson.annotation.JsonTypeInfo

import groovy.util.logging.Slf4j

/**
 * The action class that has the information required to run a given action.
 */
@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=JsonTypeInfo.As.EXISTING_PROPERTY, property="action", visible=true)
@Slf4j
abstract class UcAdfAction extends UcdObject {
	// Initalize any properties that are optional. Leave null if they are required.
	
	/**
	 * The name of the action to run.
	 */
	String action
	
	/**
	 * This flag indicates if action debug should be output.
	 */
	Boolean actionDebug = false
	

	/**
	 * Indicate if action information should be output.
	 */
	Boolean actionInfo = true
	
	/**
	 * Indicate if the action processing should be verbose.
	 */
	Boolean actionVerbose = true
	
	/**
	 * Evaluate as a Groovy code snippet to determine if the action should be run.
	 */
	String when = ""
	
	/**
	 * The variable name in which the action return value should be stored.
	 */
	String actionReturnPropertyName = ""
	
	/**
	 * The UCD URL. If left blank then will be set by the {@link #actionsRunner}.
	 */
	String ucdUrl = ""
	
	/**
	 * The UCD user ID. Can be PasswordIsAuthToken or if blank then PasswordIsAuthToken is assumed.
	 * If left blank then will be set by the {@link #actionsRunner}.
	 */
	String ucdUserId = ""
	
	/**
	 * The UCD user password or auth token. If left blank then will be set by the {@link #actionsRunner}.
	 */
 	UcAdfSecureString ucdUserPw = new UcAdfSecureString("")

	/**
	 * The UCD auth token. If left blank then will be set by the {@link #actionsRunner}.
	 */
	UcAdfSecureString ucdAuthToken = new UcAdfSecureString("")

	/**
	 * Property values provided to the action.
	 */
	Map<String, Object> propertyValues = new TreeMap<String, Object>()

	/**
	 * Property files provided to the action.
	 */
	List<UcAdfActionPropertyFile> propertyFiles = []

	/**
	 * The UCD session to use for this action. Set by the {@link #actionsRunner}.
	 * Had to make ucdSession public to work around problem of static usage of UcdSession properties causing stack overflow.
	 */
	public UcdSession ucdSession

	/**
	 * The list of properties excluded from required.
	 */
	private final static List<String> EXCLUDED_PROPS = [ 'ucdSession' ]
	
	/**
	 * The actions runner invoking this action.
	 */
	UcAdfActionsRunner actionsRunner

	/**
	 * The properties to return for a plugin invocation.
	 */
	Properties outProps = new Properties()
		
	/**
	 * The action run method overridden by all action classes.
	 */
	abstract Object run()

	/**
	 * @param ucdSession The UCD session for the action.
	 */
	public void setUcdSession(final UcdSession ucdSession) {
		this.ucdSession = ucdSession
	}
	
	/**
	 * Validate that the required action properties exist. All properties must not be null.
	 */
	public void validatePropsExist() {
		validatePropsExist([], [])
	}
	
	/**
	 * Validate that the required action properties exist. Specified properties must not be null.
	 * @param includeProps The list of property names that must be validated to assure they have values provided.
	 */
	public void validatePropsExistInclude(final List<String> includeProps) {
		validatePropsExist(includeProps, [])
	}
	
	/**
	 * Validate that the required action properties exist. Specified properties may be null.
	 * @param excludeProps The list of property names that will not be validated to assure they have values provided.
	 */
	public void validatePropsExistExclude(final List<String> excludeProps) {
		validatePropsExist([], excludeProps)
	}
	
	/**
	 * Validate that the required action properties exist.	
	 * @param includeProps The list of property names that must be validated to assure they have values provided.
	 * @param excludeProps The list of property names that will not be validated to assure they have values provided.
	 */
	public void validatePropsExist(
		final List<String> includeProps,
		final List<String> excludeProps) {

		Boolean invalidProps = false
		for (propName in this.properties.keySet()) {
			if (this.properties[propName] != null) {
				continue
			}
			
			// Skip excluded property names.
			if (excludeProps.contains(propName) || EXCLUDED_PROPS.contains(propName)) {
				continue
			}

			// If include property names provided and property name is not one of them then skip it.
			if (includeProps.size() > 0 && !includeProps.contains(propName)) {
				continue
			}
			
			log.info "Action property [$propName] not provided."
			invalidProps = true
		}

		if (invalidProps) {
			throw new UcAdfInvalidValueException("Required properties not specified for action.")
		}
	}
	
	/**
	 * Show the action properties.
	 * Showing the properties is enabled/disabled by {@link #actionInfo}.
	 */
	public void showProperties() {
		if (actionInfo) {
			// Show the action properties in sorted order.
			for (propName in this.properties.sort()*.key) {
				// Suppress showing property names that should never be shown.
				if (UcAdfActionPropertyEnum.isSuppressedPropertyName(propName)) {
					continue
				}
				
				Object propValue = this.properties.get(propName)
				
				// Suppress showing properties with empty property values.
				if ((propValue instanceof List && (propValue as List).size() < 1) || !propValue) {
					if (UcAdfActionPropertyEnum.WHEN.getPropertyName().equals(propName) || UcAdfActionPropertyEnum.isSuppressedEmptyPropertyName(propName)) {
						continue
					}
				}
				
				String outputStr = new UcAdfObjectMapper().writer().writeValueAsString(propValue)
				
				println "-> $propName=[$outputStr]"
			}
		}
	}
	
	/**
	 * Show the end of action processing information.
	 * Showing the properties is enabled/disabled by {@link #actionInfo}.
	 */
	public void end() {
		if (actionInfo && actionsRunner) {
			println "End [" + actionsRunner.getActionsStack().join("->") + "]."
		}
	}

	/**
	 * Log a message with log.info.
	 * Logging of these messages is enabled/disabled by {@link #actionInfo}.
	 * @param message the message to log.
	 */
	public void logInfo(final String message) {
		if (actionInfo) {
			log.info message
		}
	}

	/**
	 * Log a message with log.info.
	 * Logging of these messages is enabled/disabled by {@link #actionVerbose}.
	 * @param message the message to log.
	 */
	public void logVerbose(final String message) {
		if (actionVerbose) {
			log.info message
		}
	}

	/**
	 * Log an action debug message with log.info.
	 * @param message the message to log.
	 */
	public void logDebug(final String message) {
		if (actionDebug) {
			log.info message
		}
	}

	/**
	 * Log a message with log.error.
	 * @param message the message to log.
	 */
	public void logError(final String message) {
		log.error message
	}
}
