#!/bin/sh
if [ -d "docs/generated" ];
then
	rm -rf docs/generated;
fi
docker create -ti --name dummy g4t3_spring-backend
docker cp dummy:/app/ docs/generated
docker rm -f dummy
