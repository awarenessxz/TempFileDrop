#!/bin/bash

# Get Path of script
SCRIPT=$(readlink -f "$0")          # Absolute path to this script, e.g. /home/user/bin/foo.sh
SCRIPTPATH=$(dirname "$SCRIPT")     # Absolute path this script is in, thus /home/user/bin
ROOTPATH=$SCRIPTPATH/..
echo "ROOT PATH = $ROOTPATH"

# Clean up persistent data and restart infra services
$SCRIPTPATH/cleanup_and_restart_infra.sh

printf "\n==================================================\n"
printf "Clean up Docker Images...\n"
printf "==================================================\n"
docker rmi -f tempfiledrop/centralized-storage-service:latest
docker rmi -f tempfiledrop/webserver:latest
docker rmi -f tempfiledrop/webapp:latest

printf "\n==================================================\n"
printf "Building Docker Images...\n"
printf "==================================================\n"
cd $ROOTPATH && $ROOTPATH/gradlew storage-service:jibDockerBuild
cd $ROOTPATH && $ROOTPATH/gradlew webserver:jibDockerBuild
docker build -t tempfiledrop/webapp:latest -f $ROOTPATH/webapp/Dockerfile $ROOTPATH/webapp
yes | docker image prune --filter label=stage=builder

printf "\n==================================================\n"
printf "Starting up all services...\n"
printf "==================================================\n"
docker-compose -f $SCRIPTPATH/docker-compose.yaml up -d