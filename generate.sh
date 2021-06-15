#!/bin/sh
if [ -d "`dirname "$0"`/docs/generated" ]
then
	rm -rf "`dirname "$0"`/docs/generated";
fi
docker create -ti --name dummy g4t3_spring-backend
docker cp dummy:/app/ "`dirname "$0"`/docs/generated"
rm "`dirname "$0"`/docs/generated/TimeGuess-1.0.5.jar"
docker rm -f dummy