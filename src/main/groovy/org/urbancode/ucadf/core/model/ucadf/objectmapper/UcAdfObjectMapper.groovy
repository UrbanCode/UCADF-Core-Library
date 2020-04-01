package org.urbancode.ucadf.core.model.ucadf.objectmapper

import com.fasterxml.jackson.databind.AnnotationIntrospector
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.introspect.AnnotationIntrospectorPair

class UcAdfObjectMapper extends ObjectMapper {
	UcAdfObjectMapper() {
		super()
		
        AnnotationIntrospector serializerIntrospector = this.getSerializationConfig().getAnnotationIntrospector()
        AnnotationIntrospector deserializerIntrospector = this.getDeserializationConfig().getAnnotationIntrospector()

        AnnotationIntrospector customSerializerIntrospector = AnnotationIntrospectorPair.pair(serializerIntrospector, new UcAdfAnnotationIntrospector())
        AnnotationIntrospector customDeserializerIntrospector = AnnotationIntrospectorPair.pair(deserializerIntrospector, new UcAdfAnnotationIntrospector())
		
        this.setAnnotationIntrospectors(customSerializerIntrospector, deserializerIntrospector)
	}
}
