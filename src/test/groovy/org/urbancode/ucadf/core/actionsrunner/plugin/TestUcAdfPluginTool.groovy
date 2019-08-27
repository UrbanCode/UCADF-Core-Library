package org.urbancode.ucadf.core.actionsrunner.plugin

import org.urbancode.ucadf.core.actionsrunner.plugin.UcAdfPluginTool

UcAdfPluginTool ucAdfPluginTool = new UcAdfPluginTool()

Properties props

println "Getting step properties."
props = ucAdfPluginTool.getStepProperties("input.props")
println "Got step properties."
props.each { k, v ->
	println "$k=$v"
}

//props = new Properties()
ucAdfPluginTool.writeOutputProperties(props, "output.props")

println "DONE"
