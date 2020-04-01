package org.urbancode.ucadf.core.model.ucadf.objectmapper

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.introspect.Annotated
import com.fasterxml.jackson.databind.introspect.NopAnnotationIntrospector
import com.fasterxml.jackson.databind.ser.std.StdSerializer

// Map a serializer/deserializer based on a custom annotation.
public class UcAdfAnnotationIntrospector extends NopAnnotationIntrospector {
    private static final long serialVersionUID = 1L;

	// If the object has a mask annotation then use the mask serializer.
    @Override
    public Object findSerializer(Annotated am) {
		Object serializer
        if (am.getAnnotation(UcAdfMasked.class)) {
            serializer = UcAdfMaskedSerializer.class
        }

        return serializer
    }

	// Custom mask serializer.	
	static public class UcAdfMaskedSerializer extends StdSerializer {
		private static final long serialVersionUID = 1L
	
		public UcAdfMaskedSerializer() {
			super(String.class)
		}
	
		@Override
		public void serialize(
			final Object object,
			final JsonGenerator jsonGenerator,
			final SerializerProvider provider) throws IOException {
			
			jsonGenerator.writeString("***")
		}
	}

//	// If the object has a mask annotation then used the mask deserializer.
//    @Override
//    public Object findDeserializer(Annotated am) {
//		Object deserializer
//		
//        if (am.getAnnotation(UcAdfMaskedString.class)) {
//            deserializer = UcAdfMaskedDeserializer.class
//        }
//
//        return deserializer
//    }
//
//	// Custom mask deserializer.	
//	static class UcAdfMaskedDeserializer extends StdDeserializer<String> {
//	    private static final long serialVersionUID = 1L
//	
//	    public UcAdfMaskedDeserializer() {
//	        super(String.class)
//	    }
//	
//	    @Override
//	    public String deserialize(
//			final JsonParser jsonParser, 
//			final DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
//			
//			return jsonParser.getValueAsString()
//	    }
//	}
}
