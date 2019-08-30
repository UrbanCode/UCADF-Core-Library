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
@JsonDeserialize(using = UcdSnapshotConfigurationApplicationDeserializer.class)
class UcdSnapshotConfigurationApplication extends UcdSnapshotConfigurationEntity {
	List<UcdSnapshotConfigurationEntity> children = []
	
	public UcdSnapshotConfigurationEnvironments getEnvironments() {
		return getChildByName(children, UcdSnapshotConfigurationEnvironments.TYPE_NAME)
	}

	public UcdSnapshotConfigurationProcesses getProcesses() {
		return getChildByName(children, UcdSnapshotConfigurationProcesses.TYPE_NAME)
	}

	public UcdSnapshotConfigurationAdHocProperties getAdHocProperties() {
		return getChildByName(children, UcdSnapshotConfigurationAdHocProperties.TYPE_NAME)
	}
	
	public show() {
		println "--> [$name] Application"
		println "\t" + this.toJsonString()
		if (getEnvironments()) { getEnvironments().show() }
		if (getProcesses()) { getProcesses().show(1) }
		if (getAdHocProperties()) { getAdHocProperties().show("Ad-Hoc Properties for Application", 1) }
	}
	
	@JsonIgnore
	public Boolean isSoftLocked() {
		Boolean environmentsLocked = getEnvironments() ? getEnvironments().isSoftLocked() : true
		Boolean processesLocked = getProcesses() ? getProcesses().isSoftLocked() : true
		Boolean adHocPropertiesLocked = getAdHocProperties() ? getAdHocProperties().isSoftLocked() : true
		Boolean locked = (environmentsLocked && processesLocked && adHocPropertiesLocked)
		log.debug "UcdSnapshotConfigurationApplication locked=$locked"
		return locked
	}
}

// Custom deserializer to handle arrays
class UcdSnapshotConfigurationApplicationDeserializer extends JsonDeserializer<UcdSnapshotConfigurationApplication> {
	@Override
	public UcdSnapshotConfigurationApplication deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
		// Parse the JSON
		ObjectMapper mapper = new ObjectMapper()
		ObjectNode node = mapper.readTree(jp)
		
		// Create the object
		UcdSnapshotConfigurationApplication application = new UcdSnapshotConfigurationApplication()
		application.id = node.get(UcdSnapshotConfigurationEntity.NODENAME_ID as String).asText()
		application.name = node.get(UcdSnapshotConfigurationEntity.NODENAME_NAME as String).asText()

		// Child arrays
		for (childNode in node.get(UcdSnapshotConfigurationEntity.NODENAME_CHILDREN as String)) {
			String listName = childNode.get(UcdSnapshotConfigurationEntity.NODENAME_NAME as String).asText()
			switch (listName) {
				case UcdSnapshotConfigurationEnvironments.TYPE_NAME:
					UcdSnapshotConfigurationEnvironments childObject = mapper.reader(UcdSnapshotConfigurationEnvironments).readValue(childNode)
					application.children.add(childObject)
					break
					
				case UcdSnapshotConfigurationProcesses.TYPE_NAME:
					UcdSnapshotConfigurationProcesses childObject = mapper.reader(UcdSnapshotConfigurationProcesses).readValue(childNode)
					application.children.add(childObject)
					break
					
				case UcdSnapshotConfigurationAdHocProperties.TYPE_NAME:
					UcdSnapshotConfigurationAdHocProperties childObject = mapper.reader(UcdSnapshotConfigurationAdHocProperties).readValue(childNode)
					application.children.add(childObject)
					break
			}
		}
		return application
	}
}
