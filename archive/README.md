# Archive design

These projects are base on the old design where webserver is used to proxy the request to the centralized storage service.

![design 1](../doc/architecture_design1.png)

## Getting Started

### Start Infrastructure Cluster

```bash
# Clean up persistent data and restart the services (Fresh State)
sudo ../infra/cleanup_and_restart.sh

# Note that rabbitmq / keycloak are not required in this setup
```

### Start Centralized Storage Service

1. Ensure that the following services are available
    - **Minio Cluster**
    - **MongoDB**
2. Start the Storage Service
    ```bash
    cd <PROJECT_ROOT_DIR>
    ./gradlew archive:storage-service:bootRun
    ```

### Start TempFileDrop.io Service

1. Ensure that the following services are available
    - **Centralized Storage Service**
    - **MongoDB**
2. Start the Web Server
    ```bash
    cd <PROJECT_ROOT_DIR>
    ./gradlew archive:webserver:bootRun
    ```
3. Start the Web Application
    ```bash
    cd <PROJECT_ROOT_DIR>/webapp
    yarn install
    yarn start
    ```
   
