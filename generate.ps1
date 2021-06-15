if (Test-Path $PSScriptRoot/docs/generated)
{  Remove-Item $PSScriptRoot/docs/generated -Recurse -Force	}
docker create -ti --name dummy g4t3_spring-backend
docker cp dummy:/app/ $PSScriptRoot/docs/generated
Remove-Item $PSScriptRoot/docs/generated/TimeGuess-1.0.5.jar
docker rm -f dummy