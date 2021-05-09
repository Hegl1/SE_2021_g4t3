#!/bin/bash

if [ "$EUID" -ne 0 ]; then
        echo "Please run as root"
        exit 1
fi

cd bleclient
LD_BIND_NOW=1 java -cp target/bleclient.jar:./lib/tinyb.jar:./target/dependencies/* at.qe.skeleton.bleclient.Main
