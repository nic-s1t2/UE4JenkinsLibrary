#!/usr/bin/groovy

package versionlabel;

/* Config Location */
def ConfigFile	= ''

def RunCommand(def Command)
{
	bat(
		returnStdout: true,
		script: Command
	)
}

def Initialise(String InProjectDir)
{
	ConfigFile	= "${InProjectDir}/Config/DefaultGame.ini"
}

def UnlockConfig()
{
	RunCommand("attrib -r ${ConfigFile}")
}

def UpdatePrefix(String NewPrefix)
{
	RunCommand("ini /s /Script/nfVersionLabelUI.VLSettings /i BuildNumberPrefix /v ${NewPrefix} ${ConfigFile}")
}

def UpdateNumber(String NewNumber)
{
	RunCommand("ini /s /Script/nfVersionLabelUI.VLSettings /i BuildNumber /v ${NewNumber} ${ConfigFile}")
}

def UpdateSuffix(String NewSuffix)
{
	RunCommand("ini /s /Script/nfVersionLabelUI.VLSettings /i BuildNumberSuffix /v _${NewSuffix} ${ConfigFile}")
}
