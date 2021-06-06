#!/bin/bash

# Get Path of script
SCRIPT=$(readlink -f "$0")          # Absolute path to this script, e.g. /home/user/bin/foo.sh
SCRIPTPATH=$(dirname "$SCRIPT")     # Absolute path this script is in, thus /home/user/bin
echo "Script is located at $SCRIPTPATH"

printf "\n==================================================\n"
printf "Shutting down all services...\n"
printf "==================================================\n"
docker-compose -f $SCRIPTPATH/mongo/docker-compose.yaml down -v
docker-compose -f $SCRIPTPATH/minio/docker-compose.yaml down -v
docker-compose -f $SCRIPTPATH/rabbitmq/docker-compose.yaml down -v
docker-compose -f $SCRIPTPATH/keycloak/docker-compose.yaml down -v

printf "\n==================================================\n"
printf "Cleaning up all persistent data...\n"
printf "==================================================\n"
rm -rf $SCRIPTPATH/mongo/storage/*
rm -rf $SCRIPTPATH/minio/storage/*
rm -rf $SCRIPTPATH/rabbitmq/storage/*

printf "\n==================================================\n"
printf "Starting up all services...\n"
printf "==================================================\n"
docker-compose -f $SCRIPTPATH/mongo/docker-compose.yaml up -d
docker-compose -f $SCRIPTPATH/minio/docker-compose.yaml up -d
docker-compose -f $SCRIPTPATH/rabbitmq/docker-compose.yaml up -d
docker-compose -f $SCRIPTPATH/keycloak/docker-compose.yaml up -d

printf "\n==================================================\n"
printf "Initializing fresh RabbitMQ setup....\n"
printf "==================================================\n"
sleep 10 # Sleep 10 seconds
echo "Initializing Exchange for Storage Service..."
python $SCRIPTPATH/rabbitmq/scripts/init_storagesvc.py --create-exchange -e storageSvcExchange
echo "Initializing Queue and Binding for TempFileDrop..."
python $SCRIPTPATH/rabbitmq/scripts/init_storagesvc.py --create-queue -q storageSvcExchange.tempfiledrop       # queue name = destination.group
python $SCRIPTPATH/rabbitmq/scripts/init_storagesvc.py --bind-queue -e storageSvcExchange -q storageSvcExchange.tempfiledrop -r tempfiledrop
