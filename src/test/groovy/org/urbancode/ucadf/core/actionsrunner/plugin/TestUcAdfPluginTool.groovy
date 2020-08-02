package org.urbancode.ucadf.core.actionsrunner.plugin

import org.urbancode.ucadf.core.actionsrunner.plugin.UcAdfPluginTool

UcAdfPluginTool ucAdfPluginTool = new UcAdfPluginTool()

Properties props

// To develop/debug the properties file decoding do the following steps.
//
// Extract an agent.zip onto the workstation and add extern jars to the Eclipse project build configuration.
//   For 7.0.5.2: securedata.jar, CommonsUtil.jar, commons-codec.jar, jettison-1.1.jar
//   For 7.0.5.3: securedata.jar, CommonsUtil.jar, commons-codec.jar, jettison.jar
//
// Log on to the agent where you will be running a Shell plugin step.
// cd to [agentDir]/var/plugins/com.urbancode.air.plugin.Shell_9_[pluginVersion]
// Add a println statement to the ./classes/com/urbancode/air/AirPluginTool.groovy file in order to show a short-lived secret key.
//		public byte[] decodeSecret(String secret) {
//          println "0:$secret"
//			if (secret != null && !secret.trim().equals("")) {
//				return getBase64Codec().decodeFromString(secret);
//			}
//			return null;
//		}
//
// Run a shell step on a UCD instance to show the contents of the input.props file:
//   uname -a
//   cat ${AGENT_HOME}/var/temp/*/input.props
// The log from this step should give you the secret and the contexts of the encrypted input.props file.
// Create an input.props file in the UCADF-Core-Library project directory and past the contents of that file from the log into the file.

// Set the secret value here.
// This secret will be valid for a short period of time after which you'll get a clock skew error and you'll have to run the shell step agani to get a new secret and new encrypted input.props file contents.
String secretVar = ""

println "Getting step properties."
props = ucAdfPluginTool.getStepProperties("input.props", secretVar)
println "Got step properties."
props.each { k, v ->
	println "$k=$v"
}

//props = new Properties()
ucAdfPluginTool.writeOutputProperties(props, "output.props")

println "DONE"
