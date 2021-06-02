#!/bin/bash

echo "Cleaning up all persistent data..."
sudo rm -rf ./mongo/storage/*
sudo rm -rf ./rabbitmq/storage/*
sudo rm -rf ./minio/storage/*
