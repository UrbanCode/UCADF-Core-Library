/**
 * This action is used to run JUnit tests.
 */
package org.urbancode.ucadf.core.action.ucadf.general

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass

import org.junit.platform.launcher.Launcher
import org.junit.platform.launcher.LauncherDiscoveryRequest
import org.junit.platform.launcher.TestPlan
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder
import org.junit.platform.launcher.core.LauncherFactory
import org.junit.platform.launcher.listeners.SummaryGeneratingListener
import org.junit.platform.launcher.listeners.TestExecutionSummary
import org.urbancode.ucadf.core.actionsrunner.UcAdfAction

class UcAdfRunJUnitTest extends UcAdfAction {
	// Action properties.
	/** The name of the class to run. */
	String className
	
	/** The properties to make available to the tests. */
	Map testProps = [:]
	
	/**
	 * Runs the action.	
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExist()
		
		// Set the system properties from the properties provided.
		System.properties.putAll(testProps)

		// Junit5 request.
		LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
			.selectors(
			selectClass(this.getClass().getClassLoader().loadClass(className))
		)
		.build()
	
		Launcher launcher = LauncherFactory.create()
		
		TestPlan testPlan = launcher.discover(request)
		
		// Executing tests
		SummaryGeneratingListener listener = new SummaryGeneratingListener()
		launcher.registerTestExecutionListeners(listener)
		
		logVerbose("Launching JUnit request.")
		launcher.execute(
			request, 
			listener
		)

		// Get the test summary.		
		TestExecutionSummary summary = listener.getSummary()
		
		Integer exitCode = summary.getTestsFailedCount() > 0 ? 1 : 0		
		logVerbose("JUnit completed exitCode=$exitCode.")

		if (exitCode) {		
			println "\n===== JUnit Test Failures ====="
			summary.printFailuresTo(new PrintWriter(System.out))
		}
		
		println "\n===== JUnit Test Summary ====="
		summary.printTo(new PrintWriter(System.out))
		
		System.exit(exitCode)
	}	
}
