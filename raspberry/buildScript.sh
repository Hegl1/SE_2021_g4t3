#!/bin/bash

if [ "$EUID" -ne 0 ]; then
        echo "Please run as root"
        exit 1
fi

cd bleclient
mvn clean package -Dmaven.test.skip=true -Dmaven.javadoc.skip=true