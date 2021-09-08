#!/bin/bash

# Get Path of script
SCRIPT=$(readlink -f "$0")          # Absolute path to this script, e.g. /home/user/bin/foo.sh
SCRIPTPATH=$(dirname "$SCRIPT")     # Absolute path this script is in, thus /home/user/bin
INFRAPATH=$SCRIPTPATH/../infra
echo "Infra path is located at $INFRAPATH"

printf "\n==================================================\n"
printf "Shutting down all Infra services...\n"
printf "==================================================\n"
docker-compose -f $INFRAPATH/mongo/local/docker-compose.yaml down -v
docker-compose -f $INFRAPATH/minio/local/docker-compose.yaml down -v
docker-compose -f $INFRAPATH/rabbitmq/docker-compose.yaml down -v
docker-compose -f $INFRAPATH/keycloak/docker-compose.yaml down -v
#docker-compose -f $INFRAPATH/opa/docker-compose.yaml down -v
#docker-compose -f $INFRAPATH/envoy/docker-compose.yaml down -v

printf "\n==================================================\n"
printf "Cleaning up all persistent data...\n"
printf "==================================================\n"
rm -rf $INFRAPATH/mongo/local/storage/*
rm -rf $INFRAPATH/minio/local/storage/*
rm -rf $INFRAPATH/rabbitmq/storage/*

printf "\n==================================================\n"
printf "Creating Docker Network...\n"
printf "==================================================\n"
docker network create tempfiledrop_bridge

printf "\n==================================================\n"
printf "Starting up all Infra services...\n"
printf "==================================================\n"
docker-compose -f $INFRAPATH/mongo/local/docker-compose.yaml up -d
docker-compose -f $INFRAPATH/minio/local/docker-compose.yaml up -d
docker-compose -f $INFRAPATH/rabbitmq/docker-compose.yaml up -d
docker-compose -f $INFRAPATH/keycloak/docker-compose.yaml up -d
#docker-compose -f $INFRAPATH/opa/docker-compose.yaml up -d
#docker-compose -f $INFRAPATH/envoy/docker-compose.yaml up -d

printf "\n==================================================\n"
printf "Initializing fresh RabbitMQ setup....\n"
printf "==================================================\n"
sleep 10 # Sleep 10 seconds
echo "Initializing Exchange for Storage Service..."
python $INFRAPATH/rabbitmq/scripts/init_storagesvc.py --create-exchange -e storageSvcExchange -d
python $INFRAPATH/rabbitmq/scripts/init_storagesvc.py --create-exchange -e minioBucketEvents -t fanout -d
echo "Initializing Queue and Binding for TempFileDrop..."
python $INFRAPATH/rabbitmq/scripts/init_storagesvc.py --create-queue -q storageSvcExchange.tempfiledrop       # queue name = destination.group
python $INFRAPATH/rabbitmq/scripts/init_storagesvc.py --bind-queue -e storageSvcExchange -q storageSvcExchange.tempfiledrop -r tempfiledrop
echo "Initializing Queue and Binding for Minio S3 Events..."
python $INFRAPATH/rabbitmq/scripts/init_storagesvc.py --create-queue -q minioBucketEvents.storage -d
python $INFRAPATH/rabbitmq/scripts/init_storagesvc.py --bind-queue -e minioBucketEvents -q minioBucketEvents.storage -r bucketlogs

