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
@JsonDeserialize(using = UcdSnapshotConfigurationResourceDeserializer.class)
class UcdSnapshotConfigurationResource extends UcdSnapshotConfigurationEntity {
	List<UcdSnapshotConfigurationPropSheet> children = []
	
	public show(Integer indent = 0) {
		println "--> [$name] Resource"
		children.each { it.show(indent + 1) }
	}
	
	@JsonIgnore
	public Boolean isSoftLocked() {
		Boolean locked = (children.size() == 0 ? true : children.every { it.isSoftLocked() })
		log.debug "UcdSnapshotConfigurationResource locked=$locked"
		return locked
	}
}

// Custom deserializer to handle arrays
class UcdSnapshotConfigurationResourceDeserializer extends JsonDeserializer<UcdSnapshotConfigurationResource> {
	@Override
	public UcdSnapshotConfigurationResource deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
		// Parse the JSON
		ObjectMapper mapper = new ObjectMapper()
		ObjectNode node = mapper.readTree(jp)

		// Create the object
		UcdSnapshotConfigurationResource resource = new UcdSnapshotConfigurationResource()
		resource.id = node.get(UcdSnapshotConfigurationEntity.NODENAME_ID as String).asText()
		resource.name = node.get(UcdSnapshotConfigurationEntity.NODENAME_NAME as String).asText()

		// Child arrays
		for (childNode in node.get(UcdSnapshotConfigurationEntity.NODENAME_CHILDREN as String)) {
			String className = childNode.get(UcdSnapshotConfigurationEntity.NODENAME_CLASSNAME as String).asText()
			switch (className) {
				case UcdSnapshotConfigurationPropSheet.CLASS_NAME:
					UcdSnapshotConfigurationPropSheet propSheet = mapper.reader(UcdSnapshotConfigurationPropSheet).readValue(childNode)
					resource.children.add(propSheet)
					break
			}
		}
		return resource
	}
}
