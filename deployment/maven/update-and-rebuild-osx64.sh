#!/bin/bash -e

cd ../..
mvn clean
mvn install

cd deployment/maven
ant -f build.xml -Dplatform=cocoa-macosx-x86_64
