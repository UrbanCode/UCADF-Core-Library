package org.urbancode.ucadf.core.actionsrunner

import org.urbancode.ucadf.core.model.ucd.system.UcdSession

/**
 * Enumeration of the names of action properties. These names are needed by the parsers and property handlers.
 */
enum UcAdfActionPropertyEnum {
	ACTIONS("actions"),
	ACTION("action"),
	ACTIONDEBUG("actionDebug"),
	ACTIONINFO("actionInfo"),
	ACTIONRETURN("actionReturn"),
	ACTIONRETURNPROPERTYNAME("actionReturnPropertyName"),
	ACTIONRETURNEXCEPTION("actionReturnException"),
	ACTIONSRUNNER("actionsRunner"),
	ACTIONPACKAGES("actionPackages"),
	PROPERTYVALUES("propertyValues"),
	PROPERTYFILES("propertyFiles"),
	UCDSESSION("ucdSession"),
	OUTPROPS("outProps"),
	WHEN("when"),
	UCADFPACKAGESDIR("ucAdfPackagesDir"),
	UCADFPACKAGEVERSIONS("ucAdfPackageVersions")
	
	// List of action properties to suppress if empty.
	private final static List<String> SUPPRESS_EMPTY_PROPERTYNAMES = [
		PROPERTYVALUES.getPropertyName(),
		PROPERTYFILES.getPropertyName(),
		OUTPROPS.getPropertyName()
	]
	
	private final static List<String> SUPPRESS_PROPNAMES = [
		"class",
		"metaClass",
		UcdSession.PROPUCDURL, 
		UcdSession.PROPUCDUSERID, 
		UcdSession.PROPUCDUSERPW,
		UcdSession.PROPUCDAUTHTOKEN,
		ACTIONRETURN.getPropertyName(),
		ACTIONRETURNPROPERTYNAME.getPropertyName(),
		ACTIONRETURNEXCEPTION.getPropertyName(),
		ACTIONSRUNNER.getPropertyName(),
		UCDSESSION.getPropertyName(),
		ACTIONPACKAGES.getPropertyName(),
		ACTIONINFO.getPropertyName()
	]
	
	private String propertyName

	// Constructor.		
	UcAdfActionPropertyEnum(final String propertyName) {
		this.propertyName = propertyName
	}
	
	public String getPropertyName() {
		return propertyName
	}
	
	public static Boolean isSuppressedEmptyPropertyName(final String propertyName) {
		return SUPPRESS_EMPTY_PROPERTYNAMES.contains(propertyName)
	}
	
	public static Boolean isSuppressedPropertyName(final String propertyName) {
		return SUPPRESS_PROPNAMES.contains(propertyName)
	}
}
