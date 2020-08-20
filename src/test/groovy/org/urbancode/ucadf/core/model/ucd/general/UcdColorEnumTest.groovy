package org.urbancode.ucadf.core.model.ucd.general

import org.junit.jupiter.api.Test

class UcdColorEnumTest {
	@Test
	public void runTests() {
		String name = "RED"
		String longName = "COLOR_RED"
		String value = "#D9182D"

		println "${(name as UcdColorEnum).getValue()}"

		println UcdColorEnum.newEnum(name).getValue()
		
		assert(UcdColorEnum.RED.name().equals(name))
		
		assert(UcdColorEnum.RED.getValue().equals(value))
	}
}
