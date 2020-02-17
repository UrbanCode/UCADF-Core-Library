/**
 * The abstract UCD object class that provides useful methods to all subclasses.
 */
package org.urbancode.ucadf.core.model.ucadf

import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

import com.fasterxml.jackson.databind.ObjectMapper

abstract class UcAdfObject implements Serializable {
	/**
	 * Get a JSON pretty print string representation of the specified object.
	 * @param object The object to be formatted with pretty print.
	 * @return The pretty print formatted string.
	 */
	public static String toJsonPrettyString(final Object object) {
		return new ObjectMapper().writer().withDefaultPrettyPrinter().writeValueAsString(object)
	}
	
	/**
	 * Get a JSON pretty print string representation of this object.
	 * @return The pretty print formatted string.
	 */
	public String toJsonPrettyString() {
		return UcAdfObject.toJsonPrettyString(this)
	}
	
	/**
	 * Get a JSON string representation of the specified object.
	 * @param object The object to be formatted.
	 * @return The formatted string.
	 */
	public static String toJsonString(final Object object) {
		return new ObjectMapper().writer().writeValueAsString(object)
	}

	/**
	 * Get a JSON string representation of this object.
	 * @return The formatted string.
	 */
	public String toJsonString() {
		return UcAdfObject.toJsonString(this)
	}
	
	/**
	 * This is a critical part of providing Jersey with a parameterized type for serializing/deserializing a list of a given class.
	 * @param T The class name.
	 * @return The parameterized type for the class.
	 */
	public static ParameterizedType getParameterizedListGenericType(T) {
		ParameterizedType parameterizedType = new ParameterizedType() {
			public Type[] getActualTypeArguments() {
				return [T] as Type[]
			}
			public Type getRawType() {
				return List.class
			}
			public Type getOwnerType() {
				return List.class
			}
			public String getTypeName() {
				return List.getTypeName()
			}
		}
		
		return parameterizedType
	}
}
