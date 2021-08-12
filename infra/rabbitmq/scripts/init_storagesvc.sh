#!/bin/bash

# Get Path of script
SCRIPT=$(readlink -f "$0")          # Absolute path to this script, e.g. /home/user/bin/foo.sh
SCRIPTPATH=$(dirname "$SCRIPT")     # Absolute path this script is in, thus /home/user/bin
echo "script is located at $SCRIPTPATH"

echo "Initializing Exchange for Storage Service..."
python $SCRIPTPATH/init_storagesvc.py --create-exchange -e storageSvcExchange -d
python $SCRIPTPATH/init_storagesvc.py --create-exchange -e minioBucketEvents -t fanout -d

# Note: Queue Name = <spring.cloud.stream.bindings.[CHANNEL].destination>.<spring.cloud.stream.bindings.[CHANNEL].group>

echo "Initializing Queue and Binding for TempFileDrop..."
python $SCRIPTPATH/init_storagesvc.py --create-queue -q storageSvcExchange.tempfiledrop
python $SCRIPTPATH/init_storagesvc.py --bind-queue -e storageSvcExchange -q storageSvcExchange.tempfiledrop -r tempfiledrop

echo "Initializing Queue and Binding for Minio S3 Events..."
python $SCRIPTPATH/init_storagesvc.py --create-queue -q minioBucketEvents.storage -d
python $SCRIPTPATH/init_storagesvc.py --bind-queue -e minioBucketEvents -q minioBucketEvents.storage -r bucketlogs
