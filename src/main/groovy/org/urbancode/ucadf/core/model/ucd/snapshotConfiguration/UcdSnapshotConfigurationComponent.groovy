package org.urbancode.ucadf.core.model.ucd.snapshotConfiguration

import groovy.util.logging.Slf4j

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.node.ObjectNode

@Slf4j
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(using = UcdSnapshotConfigurationComponentDeserializer.class)
class UcdSnapshotConfigurationComponent extends UcdSnapshotConfigurationEntity {
	List<UcdSnapshotConfigurationEntity> children = []
	
	public UcdSnapshotConfigurationProcesses getProcesses() {
		return getChildByName(children, UcdSnapshotConfigurationProcesses.TYPE_NAME)
	}
	
	public UcdSnapshotConfigurationVersions getVersions() {
		return getChildByName(children, UcdSnapshotConfigurationVersions.TYPE_NAME)
	}

	public UcdSnapshotConfigurationAdHocProperties getAdHocProperties() {
		return getChildByName(children, UcdSnapshotConfigurationAdHocProperties.TYPE_NAME)
	}
	
	public UcdSnapshotConfigurationTemplateProperties getTemplateProperties() {
		return getChildByName(children, UcdSnapshotConfigurationTemplateProperties.TYPE_NAME)
	}
	
	public UcdSnapshotConfigurationTemplates getConfigurationTemplates() {
		return getChildByName(children, UcdSnapshotConfigurationTemplates.TYPE_NAME)
	}

	public show(Integer indent = 0) {
		println "--> [$name] Component"
		println "\t" + this.toJsonString()
		if (getProcesses()) { getProcesses().show(indent + 1) }
		if (getVersions()) { getVersions().show(indent) }
		if (getAdHocProperties()) { getAdHocProperties().show("Ad-Hoc Properties for Component", indent + 1) }
		if (getTemplateProperties()) { getTemplateProperties().show("template Properties", indent + 1) }
		if (getConfigurationTemplates()) { getConfigurationTemplates().show(indent + 1) }
	}
	
	@JsonIgnore
	public Boolean isSoftLocked() {
		Boolean processesLocked = getProcesses() ? getProcesses().isSoftLocked() : true
		Boolean versionsLocked = getVersions() ? getVersions().isSoftLocked() : true
		Boolean adHocPropertiesLocked = getAdHocProperties() ? getAdHocProperties().isSoftLocked() : true 
		Boolean templatePropertiesLocked = getTemplateProperties() ? getTemplateProperties().isSoftLocked() : true 
		Boolean configurationTemplatesLocked = getConfigurationTemplates() ? getConfigurationTemplates().isSoftLocked() : true
		Boolean locked = (processesLocked && versionsLocked && adHocPropertiesLocked && templatePropertiesLocked && configurationTemplatesLocked)
		log.debug "UcdSnapshotConfigurationComponent locked=$locked"
		return locked
	}
}

// Custom deserializer to handle arrays
class UcdSnapshotConfigurationComponentDeserializer extends JsonDeserializer<UcdSnapshotConfigurationComponent> {
	@Override
	public UcdSnapshotConfigurationComponent deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
		// Parse the JSON
		ObjectMapper mapper = new ObjectMapper()
		ObjectNode node = mapper.readTree(jp)
		
		// Create the object
		UcdSnapshotConfigurationComponent component = new UcdSnapshotConfigurationComponent()
		component.id = node.get(UcdSnapshotConfigurationEntity.NODENAME_ID as String).asText()
		component.name = node.get(UcdSnapshotConfigurationEntity.NODENAME_NAME as String).asText()

		// Child arrays
		for (childNode in node.get(UcdSnapshotConfigurationEntity.NODENAME_CHILDREN as String)) {
			String listName = childNode.get(UcdSnapshotConfigurationEntity.NODENAME_NAME as String).asText()
			switch (listName) {
				case UcdSnapshotConfigurationAdHocProperties.TYPE_NAME:
					UcdSnapshotConfigurationAdHocProperties childObject = mapper.reader(UcdSnapshotConfigurationAdHocProperties).readValue(childNode)
					component.children.add(childObject)
					break
					
				case UcdSnapshotConfigurationTemplateProperties.TYPE_NAME:
					UcdSnapshotConfigurationTemplateProperties childObject = mapper.reader(UcdSnapshotConfigurationTemplateProperties).readValue(childNode)
					component.children.add(childObject)
					break
					
				case UcdSnapshotConfigurationProcesses.TYPE_NAME:
					UcdSnapshotConfigurationProcesses childObject = mapper.reader(UcdSnapshotConfigurationProcesses).readValue(childNode)
					component.children.add(childObject)
					break
					
				case UcdSnapshotConfigurationVersions.TYPE_NAME:
					UcdSnapshotConfigurationVersions childObject = mapper.reader(UcdSnapshotConfigurationVersions).readValue(childNode)
					component.children.add(childObject)
					break
					
				case UcdSnapshotConfigurationTemplates.TYPE_NAME:
					UcdSnapshotConfigurationTemplates childObject = mapper.reader(UcdSnapshotConfigurationTemplates).readValue(childNode)
					component.children.add(childObject)
					break
			}
		}
		return component
	}
}
