package org.urbancode.ucadf.core.model.ucd.snapshotConfiguration

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.annotation.JsonIgnore

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(using = UcdSnapshotConfigurationComponentTemplateDeserializer.class)
class UcdSnapshotConfigurationComponentTemplate extends UcdSnapshotConfigurationEntity {
	List<UcdSnapshotConfigurationProcesses> children = []
	
	public UcdSnapshotConfigurationProcesses getProcesses() {
		return getChildByName(children, UcdSnapshotConfigurationProcesses.TYPE_NAME)
	}
	
	public show(Integer indent = 0) {
		println "\t".multiply(indent) + "--> [$name]"
		println "\t".multiply(indent + 1) + this.toJsonString()
		getProcesses().show(indent + 1)
	}

	@JsonIgnore
	public Boolean isSoftLocked() {
		// For some reason the template child processes don't show as locked even when they are
		return true
	}
}

// Custom deserializer to handle arrays
class UcdSnapshotConfigurationComponentTemplateDeserializer extends JsonDeserializer<UcdSnapshotConfigurationComponentTemplate> {
	@Override
	public UcdSnapshotConfigurationComponentTemplate deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
		// Parse the JSON
		ObjectMapper mapper = new ObjectMapper()
		ObjectNode node = mapper.readTree(jp)
		
		// Create the object
		UcdSnapshotConfigurationComponentTemplate componentTemplate = new UcdSnapshotConfigurationComponentTemplate()
		componentTemplate.id = node.get(UcdSnapshotConfigurationEntity.NODENAME_ID as String).asText()
		componentTemplate.name = node.get(UcdSnapshotConfigurationEntity.NODENAME_NAME as String).asText()

		// Child arrays
		for (childNode in node.get(UcdSnapshotConfigurationEntity.NODENAME_CHILDREN as String)) {
			String listName = childNode.get(UcdSnapshotConfigurationEntity.NODENAME_NAME as String).asText()
			switch (listName) {
				case UcdSnapshotConfigurationProcesses.TYPE_NAME:
					UcdSnapshotConfigurationProcesses childObject = mapper.reader(UcdSnapshotConfigurationProcesses).readValue(childNode)
					componentTemplate.children.add(childObject)
					break
			}
		}
		return componentTemplate
	}
}
