#!/bin/bash -e

cd ../..
mvn clean
mvn install

cd deployment/maven
ant
