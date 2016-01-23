#!/usr/bin/env bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$DIR/../"
mvn clean install && cd test/ && java -Xmx2G -jar spigot-1.8.8-R0.1-SNAPSHOT.jar
