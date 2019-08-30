package org.urbancode.ucadf.core.model.ucd.general

import org.junit.jupiter.api.Test

class UcdColorEnumTest {
	@Test
	public void runTests() {
		String name = "RED"
		String longName = "COLOR_RED"
		String value = "#D9182D"
		
		assert(UcdColorEnum.RED.name().equals(name))
		
		assert(UcdColorEnum.RED.getValue().equals(value))
	}
}
