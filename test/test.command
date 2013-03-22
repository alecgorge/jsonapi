#!/usr/bin/env bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$DIR/../"
mvn clean install && cd test/ && java -Xmx256M -jar craftbukkit.jar
