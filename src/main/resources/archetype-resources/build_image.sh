#!/bin/bash
cd "$(dirname "$0")"
# check mvn binary installed/available
command -v mvn >/dev/null 2>&1 || { echo >&2 "mvn binary not found"; exit 1; }
# read default properties and export them
set -a
. caas2docker.properties
# read IOM platform version from project property
export IOM_VERSION=$(mvn help:evaluate -Dexpression=platform.version -q -DforceStdout -f pom.xml)
set +a
# download and unzip CaaS2Docker
mvn org.apache.maven.plugins:maven-dependency-plugin:2.10:get -Dtransitive=false -Dartifact=com.intershop.oms:caas2docker:${IOM_VERSION}:zip -Ddest=c2d/c2d.zip -f pom.xml
unzip -uo c2d/c2d.zip -d c2d
rm -rf $CAAS_PROJECT_DIR
mkdir $CAAS_PROJECT_DIR
# extract locally built project artifacts
cat target/*.tgz | tar -C $CAAS_PROJECT_DIR -zxf - -i
# run CaaS2Docker app+config
./c2d/caas2docker/app/build.sh --docker-opts="--no-cache"
./c2d/caas2docker/config/build.sh --docker-opts="--no-cache"
