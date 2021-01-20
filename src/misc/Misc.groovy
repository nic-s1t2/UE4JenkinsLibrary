#!/usr/bin/groovy

package misc;

def Initialise()
{
	echo "Misc Lib initialised"
}

def RunCommand(def Command)
{
	bat(script: Command, returnStdout: true)
}

def BuildVsProject(String msbuildPath, String vsProjPath, String configuration)
{
	RunCommand("\"${msbuildPath}\" \"${vsProjPath}\" -t:rebuild -r -p:\"Configuration=${configuration}\"")
}

def WriteFile(String outputFile, String content)
{
	writeFile file: outputFile, text: content
}

return this
