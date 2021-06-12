#!/bin/bash

# Get Path of script
SCRIPT=$(readlink -f "$0")          # Absolute path to this script, e.g. /home/user/bin/foo.sh
SCRIPTPATH=$(dirname "$SCRIPT")     # Absolute path this script is in, thus /home/user/bin
INFRAPATH=$SCRIPTPATH/../infra
echo "Script is located at $SCRIPTPATH"

printf "\n==================================================\n"
printf "Shutting down all services...\n"
printf "==================================================\n"
docker-compose -f $INFRAPATH/mongo/docker-compose.yaml down -v
docker-compose -f $INFRAPATH/minio/docker-compose.yaml down -v
docker-compose -f $INFRAPATH/rabbitmq/docker-compose.yaml down -v
docker-compose -f $INFRAPATH/keycloak/docker-compose.yaml down -v
docker-compose -f $SCRIPTPATH/docker-compose.yaml down -v

printf "\n==================================================\n"
printf "Cleaning up all persistent data...\n"
printf "==================================================\n"
rm -rf $INFRAPATH/mongo/storage/*
rm -rf $INFRAPATH/minio/storage/*
rm -rf $INFRAPATH/rabbitmq/storage/*

printf "\n==================================================\n"
printf "Cleaning up Docker Resources...\n"
printf "==================================================\n"
yes | docker container prune
yes | docker image prune -a
yes | docker network prune
yes | docker volume prune

