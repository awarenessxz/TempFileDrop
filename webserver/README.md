# Web Server

This is the dedicated Backend web server for TempFileDrop.io built using Spring Boot Framework. The main tasks of this server is
to serve data, handle logins & registration and process file uploads/downloads.

- [Usage](#usage)
    - [Configuring application.yaml](#configuring-applicationyaml)
    - [Running server locally](#running-the-server-locally)
- [Documentation](#documentation)
    - [API Endpoints](#api-endpoints)
    - [Database Tables](#database-tables)
    - [Testing File Uploads](#testing-file-uploads-api)

## Usage

### Configuring application.yaml

| Property Group | Property | Remarks |
| --- | --- | --- |
| tempfiledrop | bucket-name | bucket name for storing files uploaded from TempFileDrop.io to storage service |
| tempfiledrop | storage-service-url | url of storage service for rest call |

### Running the server locally

```bash
# Go project's root
cd <ROOT>

# Ensure that exchange already exists. If not
python infra/rabbitmq/scripts/init_storagesvc.py --create-exchange -e storageSvcExchange

# Create the queue and bind to exchange
python infra/rabbitmq/scripts/init_storagesvc.py --create-queue -q storageSvcExchange.tempfiledrop
python infra/rabbitmq/scripts/init_storagesvc.py --bind-queue -e storageSvcExchange -q storageSvcExchange.tempfiledrop -r tempfiledrop

# Start the service
./gradlew webserver:bootRun
```

## Documentation

### API Endpoints

| Endpoints | Description |
| --- | --- |
| /api/user-info | used for mocking login |
| /api/users-upload-info | List & Delete uploads (both records in users_uploads table as well as objects itself) |

### Database Tables

| Table | Columns | Description |
| --- | --- | --- |
| users | id, username, password, creationDate | used for mocking login |
| users_upload_info | id, folder, storageId | {users to uploads} mapping. Information about the active uploads that users have which is used to display in Dashboard page. **StorageId** is the reference ID used to obtain the "files" itself from the storage service. |
