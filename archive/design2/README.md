# Design v2 - Direct Consumption with Event Feedback

![design 1](../../doc/architecture_design2b.png)

This design is an upgrade to the first implementation. Additional features such as **keycloak authentication** and 
**event feedback using rabbitmq** was added. For more details, check out the [centralized storage service documentation](storage-service).

## Features

- Multipart File Upload
    - Single / Multiple Files
    - Set metadata such as max downloads / expiry period
    - Streaming uploads with apache file upload library
    - Anonymous Upload
- Download 
    - Single / Multiple Files (zip)
    - Prevents download if file expired or have reached maximum downloads
    - Anonymous Download
- Delete Files
- Scheduled File Clean up
- Abstraction over Storage Medium
    - File Storage
    - Object Storage (MinIO)
- Event Feedback with RabbitMQ
    - Include event data & routing key when uploading/downloading/deleting files to trigger events to be published to 
    message queues.
    - Consumers subscribe to message queues to get event feedback
- Keycloak Authentication

## Getting Started

Ensure that you have met the prerequisites below before using the guides below to start the project
- If you just want to start the project and view it, go to [quick start](#for-quick-start)
- If you want to work on development, go to [development](#for-development)

### Prerequisites

You will probably need to have the following installed
- Java 11
- Node 12
- Yarn
- Python + pip
- Docker + docker-compose

### For Quick Start

If you are just intending to run the project and test the features, run the scripts below. This will use docker-compose 
to start up all services required to get the minimal set up running.

```bash
# Install python module is you have yet to
pip install pika

# Quick Start the project
cd archive/design2
sudo ./quick_start_project.sh
```

Once you are done, you can clean up the project using the following script

```bash
cd <ROOT>
sudo scripts/purge_project.sh
```

#### Quick Start Docker-Compose Setup

![Quick Start setup](../../doc/docker-compose.png)

Check out the following endpoints:

```bash
# Browser
http://localhost:3000       - tempfiledrop web application (login = user:password)    -- ENTRY POINT
http://localhost:8080       - keycloak admin console (login = admin:admin)
http://localhost:15672      - rabbitmq console (login = admin:admin123)
http://localhost:9000       - minio console (login = minio:minio123)

# Rest Endpoints
http://localhost:7001       - tempfiledrop web server
http://localhost:8801       - centralized storage service

# Docker Containers
docker exec -it mongo_server bash       - mongo database (login = root:1234)
```

### For Development 

For active development, follow the steps below to get the environment set up

#### Start Infrastructure Cluster

```bash
# Clean up persistent data and restart the infra services (Fresh State)
cd <ROOT>
sudo scripts/cleanup_and_restart_infra.sh
```

#### Start Centralized Storage Service

1. Ensure that the following services are available
    - **Minio Cluster**
    - **RabbitMQ**
    - **MongoDB**
    - **Keycloak**
2. Ensure that Exchange have been created
   ```bash
   # Create exchange if not created
   python infra/rabbitmq/scripts/init_storagesvc.py --create-exchange -e storageSvcExchange
   ```    
3. Start the Storage Service
    ```bash
    cd <ROOT>
    ./gradlew archive/design2/storage-service:bootRun
    ```

#### Start TempFileDrop.io Service

1. Ensure that the following services are available
    - **Centralized Storage Service**
    - **RabbitMQ**
    - **MongoDB**
    - **Keycloak**
2. Ensure that Queue binds to Exchange
    ```bash
    # Bind Queue to Exchange if not configured
    python infra/rabbitmq/scripts/init_storagesvc.py --create-queue -q storageSvcExchange.tempfiledrop
    python infra/rabbitmq/scripts/init_storagesvc.py --bind-queue -e storageSvcExchange -q storageSvcExchange.tempfiledrop -r tempfiledrop
    ```
3. Start the Web Server
    ```bash
    cd <ROOT>
    ./gradlew archive/design2/webserver:bootRun
    ```
4. Start the Web Application
    ```bash
    cd archive/design2/webapp
    yarn install
    yarn start
    ```
