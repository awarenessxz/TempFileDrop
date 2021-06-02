#!/bin/bash

# Get Path of script
SCRIPT=$(readlink -f "$0")          # Absolute path to this script, e.g. /home/user/bin/foo.sh
SCRIPTPATH=$(dirname "$SCRIPT")     # Absolute path this script is in, thus /home/user/bin
echo "script is located at $SCRIPTPATH"

echo "Cleaning up all persistent data..."
sudo rm -rf $SCRIPTPATH/mongo/storage/*
sudo rm -rf $SCRIPTPATH/rabbitmq/storage/*
sudo rm -rf $SCRIPTPATH/minio/storage/*
