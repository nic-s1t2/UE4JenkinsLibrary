#!/usr/bin/groovy

package versionlabel;

/* Config Location */
def ConfigFile	= ''

def RunCommand(def Command)
{
	if(isUnix())
	{
		sh( returnStdout: true, script: Command)
	}
	else
	{
		bat( returnStdout: true, script: Command)
	}
}

def Test()
{
	echo "Poop"
	//Runtime.runtime.exec(Test.bat)
}

def Initialise(String InProjectDir)
{
	ConfigFile	= "${InProjectDir}/Config/DefaultGame.ini"
}

def UnlockConfig()
{
	if(isUnix())
	{
		RunCommand("sudo chmod u=rw,g=rw,o=rw \"${ConfigFile}\"")
	}
	else
	{
		RunCommand("attrib -r ${ConfigFile}")
	}
}

def UpdatePrefix(String NewPrefix)
{
	if(isUnix())
	{
		RunCommand("sed -i '' \"/BuildNumberPrefix=/s/=.*/=${NewPrefix}/\" ${ConfigFile}")
	}
	else
	{
		RunCommand("ini /s /Script/nfVersionLabelUI.VLSettings /i BuildNumberPrefix /v ${NewPrefix} ${ConfigFile}")
	}
}

def UpdateNumber(String NewNumber)
{
	if(isUnix())
	{
		RunCommand("sed -i '' \"/BuildNumber=/s/=.*/=${NewNumber}/\" ${ConfigFile}")
	}
	else
	{
		RunCommand("ini /s /Script/nfVersionLabelUI.VLSettings /i BuildNumber /v ${NewNumber} ${ConfigFile}")
	}
}

def UpdateSuffix(String NewSuffix)
{
	if(isUnix())
	{
		RunCommand("sed -i '' \"/BuildNumberSuffix=/s/=.*/=_${NewSuffix}/\" ${ConfigFile}")
	}
	else
	{
		RunCommand("ini /s /Script/nfVersionLabelUI.VLSettings /i BuildNumberSuffix /v _${NewSuffix} ${ConfigFile}")
	}
}

return this
