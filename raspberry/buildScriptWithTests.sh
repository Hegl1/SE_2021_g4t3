#!/bin/bash

if [ "$EUID" -ne 0 ]; then
        echo "Please run as root"
        exit 1
fi

cd bleclient
export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-armhf/
mvn clean package