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
@JsonDeserialize(using = UcdSnapshotConfigurationEnvironmentDeserializer.class)
class UcdSnapshotConfigurationEnvironment extends UcdSnapshotConfigurationEntity {
	List<UcdSnapshotConfigurationPropSheet> children = []
	
	public UcdSnapshotConfigurationComponentProperties getComponentProperties() {
		return getChildByName(children, UcdSnapshotConfigurationComponentProperties.TYPE_NAME)
	}
	
	public UcdSnapshotConfigurationApprovalProcess getApprovalProcess() {
		return getChildByName(children, UcdSnapshotConfigurationApprovalProcess.TYPE_NAME)
	}
	
	public UcdSnapshotConfigurationAdHocProperties getAdHocProperties() {
		return getChildByName(children, UcdSnapshotConfigurationAdHocProperties.TYPE_NAME)
	}
	
	public show() {
		println "\t\t--> [$name] Environment"
		println "\t\t\t" + this.toJsonString()
		if (getComponentProperties()) { getComponentProperties().show() }
		if (getApprovalProcess()) { getApprovalProcess().show() }
		if (getAdHocProperties()) { getAdHocProperties().show("Ad-Hoc Properties for Environment", 3) }
	}
	
	@JsonIgnore
	public Boolean isSoftLocked() {
		Boolean componentPropertiesLocked = getComponentProperties() ? getComponentProperties().isSoftLocked() : true
		Boolean approvalProcessLocked = getApprovalProcess() ? getApprovalProcess().isSoftLocked() : true
		Boolean adHocPropertiesLocked = getAdHocProperties() ? getAdHocProperties().isSoftLocked() : true
		Boolean locked = (componentPropertiesLocked && approvalProcessLocked && adHocPropertiesLocked)
		log.debug "UcdSnapshotConfigurationEnvironment locked=$locked"
		return locked
	}
}

// Custom deserializer to handle arrays
class UcdSnapshotConfigurationEnvironmentDeserializer extends JsonDeserializer<UcdSnapshotConfigurationEnvironment> {
	@Override
	public UcdSnapshotConfigurationEnvironment deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
		// Parse the JSON
		ObjectMapper mapper = new ObjectMapper()
		ObjectNode node = mapper.readTree(jp)

		// Create the object
		UcdSnapshotConfigurationEnvironment resource = new UcdSnapshotConfigurationEnvironment()
		resource.id = node.get(UcdSnapshotConfigurationEntity.NODENAME_ID as String).asText()
		resource.name = node.get(UcdSnapshotConfigurationEntity.NODENAME_NAME as String).asText()

		// Child arrays
		for (childNode in node.get(UcdSnapshotConfigurationEntity.NODENAME_CHILDREN as String)) {
			String listName = childNode.get(UcdSnapshotConfigurationEntity.NODENAME_NAME as String).asText()
			switch (listName) {
				case UcdSnapshotConfigurationComponentProperties.TYPE_NAME:
					UcdSnapshotConfigurationComponentProperties propSheet = mapper.reader(UcdSnapshotConfigurationComponentProperties).readValue(childNode)
					resource.children.add(propSheet)
					break
					
				case UcdSnapshotConfigurationApprovalProcess.TYPE_NAME:
					UcdSnapshotConfigurationApprovalProcess propSheet = mapper.reader(UcdSnapshotConfigurationApprovalProcess).readValue(childNode)
					resource.children.add(propSheet)
					break
					
				case UcdSnapshotConfigurationAdHocProperties.TYPE_NAME:
					UcdSnapshotConfigurationAdHocProperties propSheet = mapper.reader(UcdSnapshotConfigurationAdHocProperties).readValue(childNode)
					resource.children.add(propSheet)
					break
			}
		}
		return resource
	}
}
