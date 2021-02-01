#!/usr/bin/groovy

package misc;

def Initialise()
{
	//why yes this is gross and hardcoded.
	//probably fine though as this installation is handled by VS Installer and is very consistent
	msbuildPath = "C:\\Program Files (x86)\\Microsoft Visual Studio\\2019\\Community\\MSBuild\\Current\\Bin\\msbuild.exe"
		
	echo "Misc Lib initialised"
}

def RunCommand(def Command)
{
	bat(script: Command, returnStdout: true)
}

def BuildVsProject(String vsProjPath, String configuration)
{
	RunCommand("\"${msbuildPath}\" \"${vsProjPath}\" -t:rebuild -r -p:\"Configuration=${configuration}\"")
}

def WriteFile(String outputFile, String content)
{
	writeFile file: outputFile, text: content
}

def ReadFile(String inputFile)
{
	return readFile file: inputFile
}

def MakeWritable(String targetFile)
{
	RunCommand("attrib -r ${targetFile}")
}

return this
