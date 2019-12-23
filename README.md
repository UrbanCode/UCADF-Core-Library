# UCADF-Core-Library
The UrbanCode Application Deployment Framework (UCADF) Core Library provides functionality to automate management of UrbanCode instances and create UCADF packages that support application deployment patterns. This functionality is provided in the form of actions that can be performed in a specified order to achieve a desired outcome (similar to event sourcing).

### Related Projects
The [UCADF-Core-Plugin](https://github.com/UrbanCode/UCADF-Core-Plugin) and [UCADF-Store](https://github.com/UrbanCode/UCADF-Store) projects provide related information.

# Key Features

## Abstraction/Isolation Layer
UrbanCode provides an extensive set of both documented, and undocumented RESTful APIs. The behavior of these APIs can change with each UrbanCode release as the product functionality is changed or new functionality is added.

The UCADF actions provide an abstraction/isolation layer that has the logic to perform a given action, e.g. create a component, predictably across versions while being backward, and forward compatible.

## Domain-Specific Language (DSL)
While UrbanCode may provide an extensive set of RESTful APIs, many UrbanCode administrators are more geared toward infrastructure support than development and as such may not have the knowledge, nor desire to write large amounts of code to use the APIs to automate a set of complex UrbanCode administration tasks.

The UCADF provides actions as a DSL that allows administrators to define a set of actions to perform using a YAML syntax.

## Complex Action Sets
The UrbanCode UI has steps that allow administrators to create processes to manage UrbanCode entities, e.g. create an application and set the application security, create a component and set the component security, create resources for the applications/components and set the security, create environments and set the security, etc. However, these processes can be time-consuming to create and may take a long time to run when managing a lot of entities as each step runs separately on an agent.

The UCADF provides the ability to define a large set of complex task actions as YAML and run those as one plugin step.

## UrbanCode Instance Management
In larger companies there can be a need to have multiple UrbanCode instances which increases the need for automation to manage those instances.

The UCADF provides actions to automate many of the tasks that would normally be done manually.

## UrbanCode Application Deployment Framework Life Cycle
The term "application deployment framework" refers to a package of UrbanCode processes and actions used to manage entities in UrbanCode and run UrbanCode processes in a predefined way for a given application type. This allows the package of functionality to be developed, tested, and released with more predictable outcomes.

The UCADF provides actions that to manage these UCADF packages and move them from one UrbanCode instance to another.

# UCADF Architecture
There are a number of parts to the UCADF architecture that make all of this possible.

| Part | Description |
|:-------- |:----------- |
| UCADF Actions Runner | The ***UCADF-Core-Library*** provides the UCADF actions runner that runs a defined set of actions. The actions runner is included in the ***UCADF-Core-Library*** JAR file. |
| UCADF Core Plugin | The UCADF Core Plugin provides the ability to use the UCADF Actions Runner to run actions from an UrbanCode process step. The plugin is available from the ***UCADF-Core-Plugin*** open source project. |
| UCADF Client | The UCADF Client (included in the ***UCADF-Core-Library***) provides the ability to use the UCADF Actions Runner from the command line. (More information below.) |
| UCADF Store | The UCADF Store is a set of utilities and a defined directory structure used to manage UCADFs across multiple UrbanCode instances. The store is available from the ***UCADF-Store*** open source project. |

# UCADF Action
A UCADF action is a Groovy/Java class that implements the action functionality using a UCADF action design pattern. THe design pattern includes the ability to:
- Provide complex property values to an action.- 
- Have one action call another action.
- Return a complex object.

There are currently 260+ actions available as part of the UCADF Core Library that has extensive ***JavaDoc***.

The ***[UCADF-Package-Test/Test](./UCADF-Package-Test/Test/Actions)*** directory contains UCADF action files used to test the UCADF-Core-Library and provide usage examples.

# UCADF Actions Runner
Many UCADF actions can be run in a given order with the ability to conditionally control the flow of the action processing. This action processing is perform by the ***UCADF-Core-Plugin*** running an UrbanCode process step, or by running the ***ucadfclient*** command line utility. In both cases they process YAML (or JSON) information that describes the actions to be performed.

## A Simple Example
This is a simple example that initializes a specified property value, then runs a comment action that displays the value of the specified property.
```
# Initialize property values.
propertyValues:
  MyProperty: "This is My Property"
  
# Run these actions.
actions:
  - action: UcAdfComment
    actionInfo: false
    comment: "MyProperty=%s"
    values:
      - "MyPropertyValue"
  - action: UcAdfComment
    actionInfo: false
    comment: "The end."   

```
This would output:<br>
```
MyProperty=MyPropertyValue
The end.
```

## Actions Runner Processing
The actions runner processor begins with the following:
* Sets the actions runner properties from the system properties.
* Sets the actions runner properties from the actions property values.
* Sets the actions runner properties from the actions property files.
* Initialize a UCD session with initial connection information properties provided to the runner.
* Runs the specified actions using the properties provided for the action.

## Actions Runner Properties
The actions runner maintains a cumulative set of properties during the life of the run. As each action is run the return value is placed in the runner's property collection where it can later be referenced. Properties are accessed using one of the following:
* ${u:propertyname} - Get the property and if not found then terminate with an error message.
* ${u?:propertyname} - Get the property and if not found then return an empty string.

Complex properties are accessed using a slash notation for each level of the property. Examples: 
* ${u:foo} - Get the "foo" property. 
* ${u:foo/bar} - Get the "foo" map value of "bar". Similar to Groovy "foo\[bar\]". 
* ${u:foo/bar/1} - Get the "foo" map value of "bar" that is an array and the 1st element of that array. Similar to Groovy "foo\[bar\]\[1\]". 
* A property name that contains slashes may be enclosed in single or double quotes, e.g. “actionReturn’/originalRequest/myProperty’”

A property may have a Groovy-type Eval method that is evaluated each time the property is used. Examples: 
* 'Eval("Dog".equals("${u:foo/bar/1}")' - Evaluates to true or false. 
* 'Eval("Dog" + "${u:foo/bar/1}")' - Concatenates two strings.

## Action Processing
Each action is processed as follows: 
* Set the actions runner properties from the actions property values. 
* Add the actions runner properties from the actions property files. 
* If UCD session properties are specified for the action then initialize a UCD session using those properties, otherwise use the actions runner UCD session. 
* Loads all of the action classes by looking through the classpath for Java packages named: ***org.urbancode.ucadf.\*.action*** Thus, it is required for custom UCADFs to use that package naming standard.
* Runs the action. 
* Return the action return value.

### Action "when" Property

Each action may have a "when" property that identifies if the action should be run. This property must evaluate to a value of true or false. 'Eval("Dog".equals("${u:foo/bar/1}")'

### Action Return Properties

When an action completes, the object returned by the action is stored as the "actionReturn" property which is then available in subsequent actions as "${u:actionReturn}"

If the action has a value defined for the "actionReturnPropertyName" then the object returned by the action is also stored as that property name, e.g. actionReturnPropertyName: "MyProperty" is available to subsequent actions as "${u:MyProperty}"

### UCD Session Properties:

The UCD session properties specify how the action(s) will connect to the UCD instance.

| Property | Description |
|:-------- |:----------- |
| ucdUrl | If no value provided for the action then use the value provided as runner properties. If no runner property exists then use the value of the AH_WEB_URL system property. |
| ucdUserId | If no value provided for the action then use the value provided as runner properties. |
| ucdUserPw | If no value provided for the action then use the value provided as runner properties. |
| ucdAuthToken | If no value provided for the action then use the value provided as runner properties. If no runner property exists then use the value of the AH_WEB_URL system property. |

### UCD Session Evaluation:
* If ucdUserId and (ucdUserPw or ucdAuthToken) are provided then those are used for the connection.
* If no ucdUserId and (ucdUserPw or ucdAuthToken) then the ucdUserId="PasswordIsAuthToken" is used for the connection.

### Controlling Action Information Output
Most actions output information about how they are processing. Setting an action's actionInfo property to false will suppress most of this information.

## General Action Examples
The UCADF Core has general actions available that control the flow of action processing and perform other utility-type actions.

### UcAdfComment
Outputs a comment using a printf-type evaluation of values.<br>
An optional "when" value be specified to determine if the action should be performed.
```
actions:
  - action: UcAdfComment
    actionInfo: false
    when: 'Eval(true)'
    comment: "My comment: %s."
    values:
      - "This is my comment"
```
Would output:<br>
```
My comment: This is my comment.
```

### UCADF Loops
The UCADF supports a variety of loop actions. These loop actions can all use the following features.
- The ***UcAdfContinue*** action can be used as a child action of a loop to determine when certain items should be skipped.

- The ***UcAdfBreak*** action can be used as a child action of a loop to determine when the loop should be terminated.

- An optional ***when*** value be specified to determine if the loop should be performed.

#### UcAdfItemsLoop
This loop type iterates over the items (may be list or map) and performs the specified actions. By default the ***"item"*** property value is set for every iteration but that property name may be overridden by specifying an ***"itemProperty"*** value.
```
actions:
  - action: UcAdfItemsLoop
    actionInfo: false
    when: 'Eval(true)'
    waitIntervalSecs: 2
    items:
      - "MyItem1"
      - "MySkipItem"
      - "MyItem2"
      - "MyBadItem"
      - "MyLastItem"
    itemKeyProperty: "myItemKey"
    itemProperty: "myItem"
    actions:
      - action: UcAdfLoopContinue
        actionInfo: false
        when: 'Eval("MySkipItem".equals("${u:myItem}"))'
      - action: UcAdfLoopBreak
        actionInfo: false
        when: 'Eval("MyBadItem".equals("${u:myItem}"))'
      - action: UcAdfComment
        actionInfo: false
        comment: "My item key is ${u:myItemKey} and item is ${u:myItem}."
```
The above would output the comment for items with a 2 second wait between them:
```
Skipping action [UcAdfLoopContinue}] when [false].
Skipping action [UcAdfLoopBreak}] when [false].
My item key is 0 and item is MyItem1.
Skipping action [UcAdfLoopContinue}] when [false].
Skipping action [UcAdfLoopBreak}] when [false].
My item key is 2 and item is MyItem2.
Skipping action [UcAdfLoopContinue}] when [false].
```
#### UcAdfWaitLoop
This loop type can be used for a wait (polling) loop. You may specify the following combination of properties:
* waitIntervalSecs and maxWaitSecs
* waitintervalSecs, maxWaitSecs, and maxTries
* waitIntervalsecs and maxTries
* maxTries

Example:
```
actions:
  - action: UcAdfWaitLoop
    waitIntervalSecs: 2 
    maxWaitSecs: 600 
    maxTries: 10 
    actions:
      - action: UcAdfLoopBreak
        when: 'Some condition here'
```
The above would retry every 2 seconds for a maximum of 600 seconds or 10 tries, whichever comes first. The UcAdfLoopBreak is used to exit the loop when the desired condition exists.

#### UcAdfCounterLoop
The UcAdfCounterLoop action may also be used for a counter loop.

Example:
```
actions:
  - action: UcAdfCounterLoop
    counterBegin: 1 
    counterChange: 1 
    counterEnd: 2 
    actions:
      - action: UcAdfComment
        actionInfo: false
        comment: "Counter control %s"
        values:
          - ${u:counterLoopControl}
```
The above would output.
```
Counter control {counterBegin=1, counterChange=1, counterEnd=2, counterValue=1}
Counter control {counterBegin=1, counterChange=1, counterEnd=2, counterValue=2}
```

#### UcAdfPageLoop
The UcAdfPageLoop action may also be used for a page loop needed for certain actions that can return a large number of items. The loop defines a property name that stores that page control information throughout the invocation of the child actions.

Example:
```
actions:
  - action: UcAdfPageLoop
    actionInfo: false
    rowsPerPage: 20
    actions:
      - action: UcdGetVersions
        actionInfo: false
        component: "${u:coreTestComp1}"
        actionReturnPropertyName: versions
      - action: UcAdfItemsLoop
        actionInfo: false
        items: ${u:versions}
        actions:
          - action: UcAdfComment
            actionInfo: false
            comment: "Component [%s] Version [%s] Page [%s]"
            values:
              - ${u:item/component/name}
              - ${u:item/name}
              - ${u:pageLoopControl/pageNumber}
```
The above would output.
```
Component [UCADFCORETEST-Comp1] Version [201909021402560343] Page [{rowsPerPage=20, pageNumber=1, rangeStart=0, rangeEnd=20, size=98, pages=5}]
Component [UCADFCORETEST-Comp1] Version [201909021402560929] Page [{rowsPerPage=20, pageNumber=1, rangeStart=0, rangeEnd=20, size=98, pages=5}]
...
```

### UcAdfRunActionsFromFile
This action will run actions contained in a file.

| Property | Description |
|:-------- |:----------- |
| fileName | The file name. |
| fileType | The file type. Values: AUTO, JSON, YAML. Default is AUTO. |
| propertyValues | Property values provided to the run files action, e.g. command line options from UcAdfClient. |
| propertyFiles | Property files provided to the run files action, e.g. command line options from UcAdfClient. |

Processing:
* Parses the file into UcAdfActions.
* If first run action then initialize the actions runner.
* If propertyValues were specified then set those in the actions runner.
* If propertyFiles were specified then add those to the actions.
* Runs the actions with the UcAdfActionsRunner.  

### UcAdfSetActionProperties
Set action properties to be used by the actions runner.

| Property | Description |
|:-------- |:----------- |
| setStatic | is true then the property value will be evaluated once then become a static value when used by subsequent actions. If setStatic is not specified or false then the property value will be evaluated each time it is referenced in subsequent actions. |
| propertyValues | Property values provided to the run files action, e.g. command line options from UcAdfClient. |

```
actions:
  - action: UcAdfSetActionProperties 
    setStatic: true 
    propertyValues:
      epoch: 'Eval(new Date().getTime())'
```
Usage note: This action may be used to set multiple properties. If one property’s value is evaluated from another property’s value in the same action then the first property’s value is not evaluated as static in the subsequent usage. In the above example, if there were a second property that used the epoch value then that second property value could end up with a different epoch value than the one set as the static value. Use two different actions to handle this scenario.

## Nested Actions
Actions may be nested.
```
actions:
  - action: UcAdfItemsLoop
    actionInfo: false
    items:
      - "MyItem1"
      - "MyItem2"
    itemProperty: "myItem"
    actions:
      - action: UcAdfWhen
        actionInfo: false
        when: 'Eval("MyItem1".equals("${u:myItem}"))'
        actions:
          - action: UcAdfItemsLoop
            actionInfo: false
            items:
              - "MyChildItem1"
              - "MyChildItem2"
            itemProperty: "myChildItem"
            actions:
              - action: UcAdfComment
                actionInfo: false
                comment: "My item is ${u:myItem} child ${u:myChildItem}."
```
This would output:
```
My item is MyItem1 child MyChildItem1.
My item is MyItem1 child MyChildItem2.
Skipping action [UcAdfWhen}] when [false].
```

## Recursive Actions
It is possible to do recursion by using the UcdRunActionsFromFile action with a when condition to have an action file run itself until the condition is met.

## Security Action Terminology
The UrbanCode security API terminology can be a bit confusing. The UCADF actions use some different terminology to (hopefully) alleviate some of that confsion.

| UCADF Term | UCD API Term | Description |
|:-------- |:----------- |:----------- |
| SecurityType | resourceType | Examples: Application, Component, Environment. |
| SecuritySubtype | resourceRole | What you see if you click on the “Type Configuration” tab. These names must be unique across all security types, e.g. Agent and Application can’t have the same subtype name. Example: The default subtype of Application is “Standard Application”. |
| SecurityPermission | resourceRole action | The specific security check boxes you see for roles. |

# The UCADF Client
The ucadfclient functionality is included in the UCADF-Core-Library JAR.

For Windows there is a ucadfclient.cmd file that is a wrapper to run the ucadfclient.

***ucadfclient command line options:***

| Option | Description |
|:-------- |:----------- |
| -f | Actions file to run. |
| -p | (Optional) Properties files to use. Multiple allowed. |
| -D | (Optional) Properties to define. Multiple allowed. |
| -d | (Optional) Output verbose debugging information. |

# The UCADF-Core-Package
The UCADF-Core-Library is published to Maven where it can be consumed as a build-time dependency when developing custom UCADF actions.

The UCADF-Core-Package is a zip file published to Maven that contains athe deployable set of files needed for runtime operation.

# Building the UCADF-Core-Package
The Maven build requires the following properties to be defined.

| Property | Description |
|:-------- |:----------- |
| MVN_REPO_ID | The Maven repository ID. |
| MVN_REPO_NAME | The Maven repository name. |
| MVN_REPO_URL | The Maven repository URL. |

Use the following commands to run the build and deploy the JAR to Maven:
```
export MVN_REPO_ID=MyRepoId
export MVN_REPO_NAME=MyRepoName
export MVN_REPO_URL=MyRepoURL

# Define the version number. If building with Jenkins the last digit could be the Jenkins ${BUILD_NUMBER}.
export UCADF_CORE_VERSION=1.0.0

# Set the version number in the pom.xml file.
mvn versions:set -DnewVersion="$UCADF_CORE_VERSION"

# Build, package, and deploy the library.
mvn -U clean deploy
```
# Publishing the UCADF-Core-Package
After that build completes then use the following commands to build the UCADF-Core-Package zip file and deploy it to Maven:
```
# Set the version number in the package-pom.xml file.
mvn -f package-pom.xml versions:set -DnewVersion="$UCADF_CORE_VERSION"

# Build and deploy the UCADF-Core-Package.zip file.
mvn -f package-pom.xml package deploy:deploy-file -Dfile="target/UCADF-Core-Package-$UCADF_CORE_VERSION.zip" -DgeneratePom=false -DgroupId=org.urbancode.ucadf.core -DartifactId=UCADF-Core-Package -Dversion="$UCADF_CORE_VERSION" -DrepositoryId="$MVN_REPO_ID" -Durl="$MVN_REPO_URL"
```

# Running Tests
The ***[UCADF-Package-Test/Test](./UCADF-Package-Test/Test)*** directory contains the UCADF action files and test data used to test the UCADF-Core-Library.

This is an example of running the actions file that runs all of the action tests.<br>

***From Eclipse***
```
Program arguments:
-f UCADF-Package-Test/Test/allActionTests.yml -DUCADF_STORE=C:/Dev/git/UCADF-Store -DucAdfInstance=ucadfdev

VM arguments:
-Dlog4j.configuration=file:log4j.properties
```
***From the Command Line***
```
ucadfclient -f UCADF-Package-Test/Test/Actions/allActionTests.yml -DUCADF_STORE=C:/Dev/git/UCADF-Store -DucAdfInstance=ucadfdev
```

# UrbanCode Deploy Versions Tested
| UCD Version | With Patches | Status |
|:------- |:------- |:------ |
| 7.0.1.2.1008304 | ucd-7.0.1.2-PH09559-DeletedTypesShowingAsAssignable<br>ucd-7.0.1.2_PH11030_deletedResourceTypesSeemlessCreation | All tests run and passed. |

# Using Notepad++ to Edit YAML
The open source Notepad++ application is one option for edition YAML file. One nice feature is that you can comment out a block of YAML lines with Ctrl+K and uncomment a block with Ctrl+Shift+K.

# License
This project is licensed under the MIT License - see the [LICENSE](../LICENSE) file for details
 