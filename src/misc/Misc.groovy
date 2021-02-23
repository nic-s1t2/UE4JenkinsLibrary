#!/usr/bin/groovy

package misc;

def Initialise(String inSevenZipPath = "C:\\Program Files\\7-Zip\\7z.exe", String inMsBuildPath = "C:\\Program Files (x86)\\Microsoft Visual Studio\\2019\\Community\\MSBuild\\Current\\Bin\\msbuild.exe")
{
	msbuildPath = "\"${inMsBuildPath}\""
	sevenZipPath = "\"${inSevenZipPath}\""
		
	echo "Misc Lib initialised"
}

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

def BuildVsProject(String vsProjPath, String configuration)
{
	if(isUnix())
	{
		RunCommand("/Library/Frameworks/Mono.framework/Versions/Current/Commands/msbuild \"${vsProjPath}\" -t:rebuild -r -p:\"Configuration=${configuration}\"")
	}
	else
	{
		RunCommand("${msbuildPath} \"${vsProjPath}\" -t:rebuild -r -p:\"Configuration=${configuration}\"")
	}
}

def WriteFile(String outputFile, String content)
{
	writeFile file: outputFile, text: content
}

def ReadFile(String inputFile)
{
	def Out = readFile file: inputFile
	return Out
}

def MakeWritable(String targetFile)
{
	if(isUnix())
	{
		RunCommand("sudo chmod -R 777 \"${targetFile}\"")
	}
	else
	{
		RunCommand("attrib -r \"${targetFile}\"")
	}
}

def CompressToRar(String source, String target)
{
	if(isUnix())
	{
		RunCommand("zip -r \"${target}\" \"${source}\"")
	}
	else
	{
		RunCommand("${sevenZipPath} a -t7z \"${target}\" \"${source}\"")
	}
}

def CopyFile(String source, String target, String file)
{
	if(isUnix())
	{
		RunCommand("sudo cp \"${source}/${file}\" \"${target}/${file}\"")
	}
	else
	{
		RunCommand("robocopy \"${source}\" \"${target}\" ${file} /IS /IT /IM")
	}
}

def CopyDirectory(String source, String target)
{
	if(isUnix())
	{
		RunCommand("sudo cp -r \"${source}\" \"${target}\"")
	}
	else
	{
		RunCommand("robocopy \"${source}\" \"${target}\" /IS /IT /IM")
	}
}

def DeleteDirectory(String target)
{
	if(isUnix())
	{
		RunCommand("sudo rm -r \"${WORKSPACE}\"")
	}
	else
	{
		RunCommand("del /S /Q /F \"${WORKSPACE}\"")
	}
}

return this
