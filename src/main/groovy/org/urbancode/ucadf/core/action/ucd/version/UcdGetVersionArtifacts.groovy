/**
 * This action gets component version artifacts.
 */
package org.urbancode.ucadf.core.action.ucd.version

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.GenericType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.component.UcdComponentTypeEnum
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucd.version.UcdVersion
import org.urbancode.ucadf.core.model.ucd.version.UcdVersionArtifact
import org.urbancode.ucadf.core.model.ucd.version.UcdVersionArtifactStandard
import org.urbancode.ucadf.core.model.ucd.version.UcdVersionArtifactZOS

class UcdGetVersionArtifacts extends UcAdfAction {
	// Action properties.
	/** The component name or ID. */
	String component
	
	/** The version name or ID. */
	String version
	
	/** The flag that indicates fail if the version is not found. Default is true. */
	Boolean failIfNotFound = true

	Integer maxDepth = Integer.MAX_VALUE
	
	Boolean showTree = false
	
	private UcdVersion ucdVersion
	private currentDepth = 0
	
	/**
	 * Runs the action.	
	 * @return The list of version artifact objects.
	 */
	@Override
	public List<UcdVersionArtifact> run() {
		// Validate the action properties.
		validatePropsExistExclude([ 'component' ])

		List<UcdVersionArtifact> artifacts
		
		if (component) {
			logVerbose("Getting component [$component] version [$version] artifacts.")
		} else {
			logVerbose("Getting version [$version] artifacts.")
		}

		// Get the version information.
		ucdVersion = actionsRunner.runAction([
			action: UcdGetVersion.getSimpleName(),
			actionInfo: false,
			actionVerbose: false,
			component: component,
			version: version,
			failIfNotFound: failIfNotFound
		])
		
		// Get the artifacts.
		if (ucdVersion) {
			artifacts = getArtifacts()
		}

		// Show the artifact tree.
		if (showTree) {
			println "=".multiply(20)
			currentDepth = -1
			showTree(artifacts)
			println "=".multiply(20)
		}
		
		return artifacts
	}

	// Get the artifacts tree.	
	public List<UcdVersionArtifact> getArtifacts(final UcdVersionArtifact parentArtifact = null) {
		List<UcdVersionArtifact> artifacts = []

		currentDepth++
		if (currentDepth < maxDepth) {
			// fileTree?rowsPerPage=10000&pageNumber=1&orderField=name&sortType=asc
			
			// Get the version object.
			WebTarget target = ucdSession.getUcdWebTarget().path("/rest/deploy/version/{versionId}/fileTree/{path}")
				.resolveTemplate("versionId", ucdVersion.getId())
				.resolveTemplate("path", (parentArtifact ? parentArtifact.getPath() : ""))
			logDebug("target=$target")
	
			Response response = target.request().get()
			if (response.getStatus() == 200) {
				// Get the list of artifacts.
				if (UcdComponentTypeEnum.STANDARD.equals(ucdVersion.getComponent().getComponentType())) {
					artifacts = response.readEntity(new GenericType<List<UcdVersionArtifactStandard>>(){})
				} else {
					artifacts = response.readEntity(new GenericType<List<UcdVersionArtifactZOS>>(){})
				}
	
				// Recursively get the child artifacts.
				for (artifact in artifacts) {
					artifact.setParentArtifact(parentArtifact)
					if (artifact.getChildren()) {
						artifact.setChildArtifacts(getArtifacts(artifact))
					}
				}
			} else {
				String errMsg = UcAdfInvalidValueException.getResponseErrorMessage(response)
				logVerbose(errMsg)
				if (response.getStatus() != 404 || failIfNotFound) {
					throw new UcAdfInvalidValueException(errMsg)
				}
			}
		}
		
		currentDepth--
				
		return artifacts
	}

	// Show the artifact tree.
	public showTree(List<UcdVersionArtifact> artifacts) {
		currentDepth++
		
		for (artifact in artifacts) {
			println "  ".multiply(currentDepth) + "${artifact.getName()} type=${artifact.getType()}"
			showTree(artifact.getChildArtifacts())
		}
		
		currentDepth--
	}
}
