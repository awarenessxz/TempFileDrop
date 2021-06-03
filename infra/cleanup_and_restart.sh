#!/bin/bash

# Get Path of script
SCRIPT=$(readlink -f "$0")          # Absolute path to this script, e.g. /home/user/bin/foo.sh
SCRIPTPATH=$(dirname "$SCRIPT")     # Absolute path this script is in, thus /home/user/bin
echo "script is located at $SCRIPTPATH"

echo "shutting down all services..."
sudo docker-compose -f $SCRIPTPATH/mongo/docker-compose.yaml down -v
sudo docker-compose -f $SCRIPTPATH/minio/docker-compose.yaml down -v
sudo docker-compose -f $SCRIPTPATH/rabbitmq/docker-compose.yaml down -v

echo "Cleaning up all persistent data..."
sudo rm -rf $SCRIPTPATH/mongo/storage/*
sudo rm -rf $SCRIPTPATH/minio/storage/*
sudo rm -rf $SCRIPTPATH/rabbitmq/storage/*

echo "starting up all services..."
sudo docker-compose -f $SCRIPTPATH/mongo/docker-compose.yaml up -d
sudo docker-compose -f $SCRIPTPATH/minio/docker-compose.yaml up -d
sudo docker-compose -f $SCRIPTPATH/rabbitmq/docker-compose.yaml up -d
