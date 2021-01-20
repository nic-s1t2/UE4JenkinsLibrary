#!/usr/bin/groovy

package unreal;

enum BuildConfiguration
{
	Development,
	Test,
	Shipping,
	DebugGame
}

def GetBuildConfigurationChoices()
{
	return Arrays.toString(BuildConfiguration.values()).replaceAll('^.|.$', "").split(", ").join("\n")
}

/* Project Specific Directories */
def EngineDir	= ''
def ProjectName = ''
def ProjectDir	= ''
def ProjectFile	= ''

/* Return BatchFiles Dir */
def BatchDir = ''
def ScriptInvocationType = ''

/* Return UBT */
def UBT	= ''

/* Return UAT */
def UAT = ''

/* Return the editor CMD */
def UE4_CMD = ''

/* Arguments to pass to all commands. e.g -BuildMachine */
def DefaultArguments = ''

def RunCommand(def Command)
{
	if(isUnix())
	{
		sh(script: Command, returnStdout: true)
	}
	else
	{
		bat(script: Command, returnStdout: true)
	}
}

/* Initialise the Object with a project name, engine directory, optional project directory and optional default arguments to pass to all commands 
 * if projectDir is not passed it is assumed the engine dir is where the project can be found
 */
def Initialise(String projectName, String engineDir, String projectDir = "", String defaultArguments = "", boolean bIsSourceEngine = false)
{
	ProjectName		= projectName
	EngineDir		= engineDir

	if(projectDir == "")
	{
		projectDir	= "${EngineDir}/${ProjectName}"
	}

	ProjectDir      = projectDir
	ProjectFile     = "\"${ProjectDir}/${ProjectName}.uproject\""

	DefaultArguments = defaultArguments
	
	BatchDir = isUnix() ? "${EngineDir}/Engine/Build/BatchFiles/Linux" : "${EngineDir}/Engine/Build/BatchFiles"
	ScriptInvocationType = isUnix() ?  "sh" : "bat"
	
	UBT	= "\"${BatchDir}/Build.${ScriptInvocationType}\""

	UAT = "\"${EngineDir}/Engine/Build/BatchFiles/RunUAT.${ScriptInvocationType}\""

	UE4_CMD = "\"${EngineDir}/Engine/Binaries/Win64/UE4Editor-Cmd.exe\""
	
	bIsSourceBuildEngine = bIsSourceEngine
}

/* Runs Setup.bat */
def Setup()
{
	RunCommand("\"${EngineDir}/Setup.${ScriptInvocationType}\" --force")
}

/* Generate Project files for the initialised project */
def GenerateProjectFiles()
{
	if(bIsSourceBuildEngine)
	{
		RunCommand("\"${BatchDir}/GenerateProjectFiles.${ScriptInvocationType}\" -projectfiles -project=${ProjectFile} -game -engine -progress ${DefaultArguments}")
	}
	else
	{
		RunCommand("${UBT} -ProjectFiles -UsePrecompiled -Game ${ProjectFile}")
	}
}

/** 
  * Compile passed in project for a given BuildConfiguration.
  *	target - The Compilation target
  *	buildConfiguration - The compilation configuration type
  * platform - the target compilation platform
  * additionalArguments - Additional arguments to pass to the compiler
 */ 
def Compile(String target, BuildConfiguration buildConfiguration, String platform = "Win64", String additionalArguments = "")
{
	RunCommand("${UBT} ${target} ${ProjectFile} ${platform} " +  buildConfiguration.name() + " ${additionalArguments} ${DefaultArguments}")
}

/** 
  * Compile passed in project for a given BuildConfiguration. 
  *	buildConfiguration - The compilation configuration type
  * editor - Whether or not this target is for editor
  * platform - the target compilation platform
  * additionalArguments - Additional arguments to pass to the compiler
 */ 
def CompileProject(BuildConfiguration buildConfiguration, boolean editor = true, String platform = "Win64", String additionalArguments = "")
{
	String projectTarget = "${ProjectName}"
	if(editor && (buildConfiguration == BuildConfiguration.Development || buildConfiguration == BuildConfiguration.DebugGame))
	{
		projectTarget += "Editor"
	}
	RunCommand("${UBT} ${projectTarget} ${ProjectFile} ${platform} " +  buildConfiguration.name() + " ${additionalArguments} ${DefaultArguments}")
}

def RunBuildGraph(String scriptPath, String target, def parameters, String additionalArguments = "")
{
	String parsedParams = ""
	parameters.each
	{
		parameter -> parsedParams += "-set:${parameter.key}=\"${parameter.value}\" "
	}

	RunCommand("${UAT} BuildGraph -Script=\"${scriptPath}\" -target=\"${target}\" -set:ProjectName=${ProjectName} -set:UProject=${ProjectFile} ${parsedParams} ${additionalArguments} ${DefaultArguments}")
}

/** 
  * Cook the project for the given platform(s)
  * iterative - Use iterative cooking
  * mapsToCook - The maps we want cooked
  *	platforms - The desired cooking platform. Each platform should be seperated by a +. e.g. WindowsNoEditor+Xbox+Linux
  * additionalArguments - Optional arguments to pass to the cooker
 */ 
def CookProject(String platforms = "WindowsNoEditor", String mapsToCook = "", boolean iterative = true, String additionalArguments = "-fileopenlog")
{
	 RunCommand("${UE4_CMD} ${ProjectFile} -run=Cook -targetplatform=${platforms} -map=${mapsToCook} ${additionalArguments} ${DefaultArguments}" + (iterative ? " -iterate -iterateshash" : ""))
}

/** 
  * Package the project for a target platform
  * platform - The platform we want to package to
  * buildConfiguration - The BuildConfiguration type of this deployment
  *	stagingDir - The staging directory we want to output this deployment to
  * usePak - Whether or not to use pak files
  * iterative - Use iterative deployment
  * cmdlineArguments - Arguments to pass to the commandline when the package next launches
  * additionalArguments - Optional arguments to pass to the deployment command
 */ 
def PackageProject(String platform, BuildConfiguration buildConfiguration, String stagingDir, boolean usePak = true, boolean iterative = true, String cmdlineArguments = "", String additionalArguments = "")
{
	RunCommand("${UAT} BuildCookRun -project=${ProjectFile} -platform=${platform} -skipcook -skipbuild -nocompileeditor -NoSubmit -stage -package -clientconfig=" + buildConfiguration.name() + " -StagingDirectory=\"${stagingDir}\"" + (usePak ? " -pak " : " ") + " -cmdline=\"${cmdlineArguments}\" " + "${additionalArguments} ${DefaultArguments}")
}

/**
  * Package and Deploy the project to a platform
  * platform - The platform we want to package and deploy to
  * buildConfiguration - The BuildConfiguration type of this deployment
  *	stagingDir - The staging directory we want to output this deployment to
  * deviceIP - The IP of the device we want to deploy to
  * usePak - Whether or not to use pak files
  * iterative - Use iterative deployment
  * cmdlineArguments - Arguments to pass to the commandline when the package next launches
  * additionalArguments - Optional arguments to pass to the deployment command
 */ 
def PackageAndDeployProject(String platform, BuildConfiguration buildConfiguration, String stagingDir, String deviceIP, boolean usePak = true, boolean iterative = true, String cmdlineArguments = "", String additionalArguments = "")
{
	PackageProject(platform, buildConfiguration, stagingDir, usePak, iterative, cmdlineArguments, " -Messaging -deploy -device=${platform}@${deviceIP} " + (iterative ? " -iterativedeploy " : " ") + additionalArguments)
}

/* Build the project's DDC, recommend to use in combation with a shared DDC https://docs.unrealengine.com/en-us/Engine/Basics/DerivedDataCache */
def BuildDDC()
{
	 RunCommand("${UE4_CMD} ${ProjectFile} -run=DerivedDataCache -fill ${DefaultArguments}")
}

/**
  * Full Build, Cook, Compile and package command, works for iOS and Windows.
  * platform - The platform we want to package for
  * buildConfiguration - the desired build config
  * stagingDir - location to output the completed build.
 */
def QuickPackage(String platform, BuildConfiguration buildConfiguration, String stagingDir)
{
	RunCommand("${UAT} BuildCookRun -project=${ProjectFile} -platform=${platform} -noP4 -allmaps -build -cook -stage -clean -pak -archive -package -clientconfig=" + buildConfiguration.name() + " -archivedirectory=\"${stagingDir}\"")
}

/**
  * Run Gauntlet test(s) on designated build and target machines as outlined in Devices.json
  * platform - target platform type, eg Win64/IOS/Android
  * buildDir - location of build to run test(s) on
  * buildConfiguration - build config type, eg Development
  * test - the test suite to run, eg DaedalicTestAutomationPlugin.Automation.DaeGauntletTest
  * reportDir - location to output reports to
  * devicesJson - path to devices.json file that outlines what devices to run the tests on
 */
def RunGauntletTest(String platform, String buildDir, BuildConfiguration buildConfiguration, String test, String reportDir, String devicesJson)
{
	RunCommand("${UAT} RunUnreal -project=\"${ProjectFile}\" -scriptdir=\"${ProjectDir}\\Build\\Scripts\" -platform=${platform} -build=\"${buildDir}\" -configuration=" + buildConfiguration.name() + " -test=\"${test}(ReportPath=\"${reportDir}\")\" -devices=\"${devicesJson}\" -veryverbose")
}

/**
  * For deploying onto Windows devices over the network
  * projectdir=<PathToUProjectFile>", "The path to your game's .uproject file
  * devices=<Devices.json>", "Devices to install on. If empty uses the local machine, ie this computer
  * configuration=<Config>", "Configuration to install/launch. Defaults to development
  * build=<path>", "Path to a folder with a build, or a folder that contains platform folders with builds in (e.g. /Saved/StagedBuilds)
  * test=<test>", "Name of test to run, as per gauntlet launch scripts eg \"DaedalicTestAutomationPlugin.Automation.DaeGauntletTest\". You can also define a test in the Devices.json per-device, and any set there will override this argument. So you could consider this argument the 'default' test to run on any devices with no test designated
  * reportdir=<test>", "Path to directory to copy all remote reports to, for local access
  * cleanup=true/false", "bool flag to delete any evidence off the target device(s). If true everything coped will be deleted, including reports. Default is false
  * cmdline=<AdditionalArguments>", "Additional command line arguments to pass to build for when it launches
 */
def WindowsDeployTest()
{
	RunCommand("${UAT} WinDeploy -projectdir=\"${projectFile}\" -devices=\"${devicesJson}\" -configuration=\"${config}\" -build=\"${buildPath}\" -test=\"${test}\" -reportdir=\"${reportDir}\" -cleanup=true -cmdline=\"${cmdLine}\"")
}

return this
