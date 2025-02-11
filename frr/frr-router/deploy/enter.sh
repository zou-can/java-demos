#!/bin/bash

EXEC="vtysh"

if [ -n "$1" ]; then
    EXEC = "$1"
fi

docker exec -it $(docker ps -q --filter "name=frr") $EXEC