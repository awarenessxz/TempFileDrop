# Design v1 - Backend Proxy

This project is the very first design that I conceptualize where I used the backend webserver to proxy file storage 
related requests to the centralized storage service. No authentication have been implemented in this design. 

![design 1](../../doc/architecture_design1.png)

## Features

- Multipart File Upload
    - Single / Multiple Files
    - Set metadata such as max downloads or expiry period
- Download 
    - Single / Multiple Files (zip)
    - Prevents download if file expired or have reached maximum downloads
- Delete Files
- Scheduled File Clean up
- Abstraction over Storage Medium
    - File Storage
    - Object Storage (MinIO)

## Getting Started

### Start Infrastructure Cluster

```bash
# Clean up persistent data and restart the services (Fresh State)
sudo ../../scripts/cleanup_and_restart_infra.sh

# Note that rabbitmq / keycloak are not required in this setup
```

### Start Centralized Storage Service

1. Ensure that the following services are available
    - **Minio Cluster**
    - **MongoDB**
2. Start the Storage Service
    ```bash
    cd <PROJECT_ROOT_DIR>
    ./gradlew archive:design1:storage-service:bootRun
    ```

### Start TempFileDrop.io Service

1. Ensure that the following services are available
    - **Centralized Storage Service**
    - **MongoDB**
2. Start the Web Server
    ```bash
    cd <PROJECT_ROOT_DIR>
    ./gradlew archive:design1:webserver:bootRun
    ```
3. Start the Web Application
    ```bash
    cd <PROJECT_ROOT_DIR>/webapp
    yarn install
    yarn start
    ```
   
