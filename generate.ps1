if (Test-Path docs/generated)
{  Remove-Item docs/generated -Recurse -Force	}
docker create -ti --name dummy g4t3_spring-backend
docker cp dummy:/app/ docs/generated
docker rm -f dummy