#!/bin/bash

if [ "$EUID" -ne 0 ]; then
        echo "Please run as root"
        exit 1
fi

#echo -en "Backend (http[s]://<ip>:<port>): "
#read BACKEND

cd bleclient
eval $(egrep -v '^#' ../.env | xargs) java -cp target/bleclient.jar:./lib/tinyb.jar:./target/dependencies/* at.qe.skeleton.bleclient.Main
