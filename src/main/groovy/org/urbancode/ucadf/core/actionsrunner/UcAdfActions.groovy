package org.urbancode.ucadf.core.actionsrunner

import org.urbancode.ucadf.core.model.ucd.general.UcdObject

import groovy.util.logging.Slf4j

// Models the actions to be run by the actions runner.
@Slf4j
public class UcAdfActions extends UcdObject {
	// The actions property values.	
	public Map<String, Object> propertyValues = new TreeMap<String, Object>()

	// The actions property files.
	public List<UcAdfActionPropertyFile> propertyFiles = []

	// The actions.
	private List<LinkedHashMap> actions = []

	// Constructors.
	UcAdfActions() {
	}	
	
	public List<HashMap> getActions() {
		return actions
	}
	
	public setActions(List<LinkedHashMap> actions) {
		this.actions = actions
	}
	
    public Map<String, String> getPropertyValues() {
		return propertyValues
	}
	
	public setPropertyValues(Map<String, String> propertyValues) {
		this.propertyValues = propertyValues
	}
	
	public addAction(Map actionMap) {
		this.actions.add(actionMap)
	}

	public setPropertyFiles(final List<UcAdfActionPropertyFile> propertyFileDefs) {
		this.propertyFiles = propertyFileDefs
	}

	public addPropertyFiles(final List<UcAdfActionPropertyFile> propertyFileDefs) {
		this.propertyFiles.addAll(propertyFileDefs)
	}
	
    public List<UcAdfActionPropertyFile> getPropertyFiles() {
		return propertyFiles
	}
}
