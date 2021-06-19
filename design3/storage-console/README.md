# Storage Console

Storage Console is a platform for managing centralized storage service. It allows administrators to grant access, create 
message queues, etc... Consumers can use this to manage their uploads.

## Getting Started

Ensure that **Centralized Storage Service** is running...

```bash
# Start the Web Server
./gradlew design3:storage-console:webserver:bootrun

# Start the Web App
yarn install
yarn start
```

## Implementation Details

### Features

#### Common

1. Login using keycloak
2. Get all buckets tagged to consumer
    - List all files inside bucket
    - Delete files action available
3. Get all services tagged to consumer
    - show routing key / bucket tagged to service

#### Admin

1. Create consumer's service
    - Create dedicated message queue
    - Populate consumer information to database
2. View all services / buckets
    - Delete services
    - Delete files / buckets
    - Edit consumer details

**Note: Keycloak roles will have to be added using Keycloak UI**

### Data Source

| Data | Source | Remarks |
| --- | --- | --- |
| consumer's info | data_consumer_info (DB) | list services / buckets tagged to consumer |
| bucket info | storage service (REST) | list of files inside bucket |
| events | data_events (DB) | list of events tagged to consumer |
