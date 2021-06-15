# TempFileDrop

**Table of Content**
- [Overview](#overview)
- [Architecture Design](#architecture-design)
    - [First Design](#first-design)
    - [Second Design](#second-design)
        - [Event Streaming Flow](#event-streaming-flow)
        - [Authentication with Keycloak](#authentication-with-keycloak)
    - [Design Considerations](#design-considerations)
- [Getting Started](#getting-started)
- [How to consume storage service](storage-service/README.md#how-to-consume-centralized-storage-service)
- [Future Works](#future-works)
- [References](#references)
    - [Command Cheat Sheet](doc/CHEATSHEET.md)
    
## Overview

There are 2 main purpose of this project.
1. **To design a centralized storage service.**
    - Storage Medium will be based on either **file storage (NAS)** or **object storage (MinIO)**
    - Storage Service acts as an **abstraction layer** such that consumers do not consume the storage medium directly.
    - Features include:
        - **Scheduled Cleanup** based on the following settings
            - **Expiry datetime** for files
            - **Max Downloads** allowed
        - **Event Feedback** when files are uploaded / downloaded / deleted
        - **Anonymous Upload / Download**
2. **To create a temporary file sharing application.**
    - **TempFileDrop.io** is a project that replicates [file.io](https://www.file.io/) which is a super simple file sharing application.
    - Understand **file uploads / downloads using Rest** 
    
**Technology Stack:**
- **React** - Website Framework
- **Nginx** - Web server to serve website
- **MinIO** - Object storage server
- **Spring Boot** - API web services
- **Rabbit MQ** - Publish / Subscribe Messages
- **Docker** - Containerization

**For more details on implementation, refer to the README of the individual services**
- [Website](webapp)
- [Web Server](webserver)
- [Storage Service](storage-service)
- **Archive (Old Designs)**
    - [Storage Service Client Library](archive/storage-service-client)
    - [Storage Service](archive/storage-service)
    - [Web Server](archive/webserver)
    - [Website](archive/webapp)

## Architecture Design

### Design Sprints

#### Version 1 - Backend Proxy

Refer to documentation in [design 1](archive/design1)

#### Version 2 - Direct Consumption with Event Feedback

Refer to documentation in [archive_2](archive/design2)

### Design Considerations

1. How much space do we need for the MinIO Cluster
2. API Service
    - Batch Calls
    - Speed of transfer
    - Mode of transfer
    - How much load can it handles (Out of Memory Issues)
3. File Upload Mechanism
    - **Option A - Direct File Upload**
        - use HTTP `Content-Type` header on request to set the proper content
            ```
            PUT /profile/image HTTP/1.1
            Content-Type: image/jpeg
            Content-Length: 284
            
            raw image content...
            ```
        - This is a straightforward method that is recommended in most cases
    - **Option B - Multipart HTTP request [SELECTED]**
        - useful to support `uploading of multiple files at once` as well as supporting `different metadata` (eg combination 
        of images and JSON) in the same request
        - We will be using a variation of this **Mixed Multipart** which is a Multipart request with json
    - **Option C - Two-step: Metadata + Upload**
        - Submit meta-data first using `POST` method and return a `201 Created` with the location of where to upload the content
        - Submit a `PUT` request to upload content
4. Possible File Upload Vulnerabilities
    - **Server Side Request Forgery Vulnerability**
    - **Defend Strategies**:
        - There should be a **whitelist** of allowed file types. This list determines the types of files that can be uploaded,
        and rejects all files that do not match the approved types.
        - **Client- or Server-side input validation** to ensure evasion techniques that have not been used to bypass the
        whitelist filter. These evasion techniques could include appending a second file type to the file name 
        (eg.. image.jph.php) or using trailing space or dots in the file name.
        - **Maximum filename length and file size** should be set.
        - Directory to which files are uploaded should be **outside of the website root**.
        - All uploaded files should be **scanned by antivirus software** before they are opened. 
        - App should not use **file names** supplied by user. Uploaded files should be renamed according to a predetermined
        condition. This makes it harder for attacker to find their uploaded files.

## Getting Started

Ensure that you have met the prerequisites below before using the guides below to start the project
- If you just want to start the project and view it, go to [quick start](#for-quick-start)
- If you want to work on development, go to [development](#for-development)
- If you want to deploy to kubernetes, go to [deployment](#for-deployment-to-kubernetes)

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
sudo scripts/quick_start_project.sh
```

Once you are done, you can clean up the project using the following script

```bash
sudo scripts/purge_project.sh
```

#### Quick Start Docker-Compose Setup

![Quick Start setup](doc/docker-compose.png)

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
    ./gradlew storage-service:bootRun
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
    ./gradlew webserver:bootRun
    ```
4. Start the Web Application
    ```bash
    cd webapp
    yarn install
    yarn start
    ```

### For Deployment to Kubernetes

#### Kubernetes Set up

![Quick Start setup](doc/kubernetes.png)

## Future Works

1. Implement Security
    - TLS (HTTPS) 
    - IAM for MinIO Cluster
    - Bucket Authorization
2. Storage Service
    - Look at Presigned Url feature that is available in S3 storage
    - Monitoring Metrics
3. Misc
    - Upgrade to Reactive Web (use webclient instead of restTemplate)
    - Make Navbar reactive to small screen (Frontend)
    - Viewing Nginx Logs in Docker Logs
    - Kubernetes Deployment (Production Mode)
        - https / wss
    - Websocket (with security)
    
## References
- [Command Cheat Sheet](doc/CHEATSHEET.md)
- API / S3 Design
    - [API Design Guidance: File Upload](https://tyk.io/api-design-guidance-file-upload/)
    - [Stackoverflow - How do web applications typically interact with amazon s3](https://stackoverflow.com/questions/54655279/how-do-web-applications-typically-interact-with-amazon-s3)
    - [How to gracefully store user files](https://stormpath.com/blog/how-to-gracefully-store-user-files)
- Authentication
    - [How to add login Authentication to React Applications](https://www.digitalocean.com/community/tutorials/how-to-add-login-authentication-to-react-applications)
    - [React Login Authentication using useContext and useReducer](https://soshace.com/react-user-login-authentication-using-usecontext-and-usereducer/)
- Multipart upload / download
    - React & Axios
        - [React File Upload/Download Example with Spring Rest Api](https://bezkoder.com/react-file-upload-spring-boot/)
        - [React File Upload/Download Example with Spring Rest Api](https://bezkoder.com/react-file-upload-spring-boot/)
        - [React-Dropzone.js](https://react-dropzone.js.org/)
            - [Tutorial Example 1](https://www.digitalocean.com/community/tutorials/react-react-dropzone)
            - [Tutorial Example 2](https://www.newline.co/@dmitryrogozhny/how-to-drag-and-drop-files-in-react-applications-with-react-dropzone--c6732c93)
            - [CSS Tricks: Drag and drop for file uploading](https://css-tricks.com/drag-and-drop-file-uploading/)
        - [Stackoverflow - How to download files using Axios](https://stackoverflow.com/questions/41938718/how-to-download-files-using-axios)
        - [Stackoverflow - How tp download file in reactjs](https://stackoverflow.com/questions/50694881/how-to-download-file-in-react-js)    
    - Spring Boot
        - Upload
            - [Spring Boot Multipart File Upload to Folder](https://bezkoder.com/spring-boot-file-upload/)
            - [Spring Boot Uploading and Downloading file from MinIO object store](https://blogs.ashrithgn.com/spring-boot-uploading-and-downloading-file-from-minio-object-store/)
            - [File Upload with Spring MVC](https://www.baeldung.com/spring-file-upload)
            - [Spring Boot File Upload/Download](https://www.devglan.com/spring-boot/spring-boot-file-upload-download)
            - [Upload Large Files with Spring Boot](https://blog.sayem.dev/2017/07/upload-large-files-spring-boot-html/)
            - [Upload large file in Spring Boot 2 application](https://dzone.com/articles/upload-large-file-in-spring-boot-2-application-usi)
            - [Advanced Spring File Upload](https://medium.com/swlh/advanced-spring-file-upload-6595d3c2b8f9)
            - [Use RestTemplate cross-service large file uploads, probably 2G](https://www.programmersought.com/article/64782425852/)
        - Download
            - [Springboot single file download, multiple files zip package](https://www.programmersought.com/article/2688897886/)
            - [Stackoverflow - Difference between return byte array and input stream](https://stackoverflow.com/questions/49050569/is-there-a-difference-between-returning-byte-array-or-servlet-output-stream-on-f)
            - [Spring Rest Template Download large file](https://www.baeldung.com/spring-resttemplate-download-large-file)
            - [Spring Boot File Download](https://o7planning.org/11765/spring-boot-file-download)
            - [Download Server](https://github.com/nurkiewicz/download-server)
            - [Download a file using Spring RestTemplate](https://www.javacodemonk.com/download-a-file-using-spring-resttemplate-75723d97)
            - [Spring MVC Image Media Data](https://www.baeldung.com/spring-mvc-image-media-data)
            - [Stackoverflow - Spring MVC Large file download (Out of Memory Issue)](https://stackoverflow.com/questions/15800565/spring-mvc-large-files-for-download-outofmemoryexception)
            - [Bad Chunk Header mystery](https://rey5137.com/bad-chunk-header-mystery/)
            - [Stackoverflow -  Get opened input stream from rest template for large file processing](https://stackoverflow.com/questions/34936101/get-opened-input-stream-from-rest-template-for-large-file-processing)
        - Upload with JSON Data
            - [RequestBody and Multipart on Spring Boot](https://blogs.perficient.com/2020/07/27/requestbody-and-multipart-on-spring-boot/)
            - [Multiple files upload with request body using spring boot and test using Postman](https://medium.com/@pankajsingla_24995/multipart-request-with-request-body-using-spring-boot-and-test-using-postman-6ea46b71b75d)
            - [Stackoverflow - React Multipart file and JSON data](https://stackoverflow.com/questions/59235491/react-ajax-request-with-multipart-file-and-json-data)
            - [Spring Rest Template Multipart Upload](https://www.baeldung.com/spring-rest-template-multipart-upload)
            - [Stackoverflow - How do I send a multipart file using spring rest template](https://stackoverflow.com/questions/55138538/how-do-i-send-a-multipartfile-using-spring-resttemplate)
- Nginx Proxy
    - [Set up proxy to work with multiple apis in create-react-app](https://create-react-app.dev/docs/proxying-api-requests-in-development/#configuring-the-proxy-manually)
- Forwarding Request/Response (Service to Service in Spring)
    - [Stackoverflow - How to send Multipart form data with restTemplate Spring-mvc](https://stackoverflow.com/questions/28408271/how-to-send-multipart-form-data-with-resttemplate-spring-mvc)
    - [Stackoverflow - How to proxy a http video stream to any number of clients](https://stackoverflow.com/questions/47277640/how-to-proxy-a-http-video-stream-to-any-amount-of-clients-through-a-spring-webse)
- Spring Boot Exception
    - [Spring Template Error Handling](https://www.baeldung.com/spring-rest-template-error-handling)
    - [Log your rest template without destroying the body](https://objectpartners.com/2018/03/01/log-your-resttemplate-request-and-response-without-destroying-the-body/)
- Swagger
    - [Documenting a Spring REST API Using OpenAPI 3.0](https://www.baeldung.com/spring-rest-openapi-documentation)
    - [Swagger Authentication](https://sourabhparsekar.medium.com/openapi-specification-swagger-authentication-c150f86748ea)
    - [Enable Authorize Button in Swagger UI](https://stackoverflow.com/questions/59898874/enable-authorize-button-in-springdoc-openapi-ui-for-bearer-token-authentication/60666209#60666209)
- Spring Cloud Stream + RabbitMQ
    - [Okta - spring cloud stream 3.0](https://developer.okta.com/blog/2020/04/15/spring-cloud-stream)
    - [Introduction to event driven microservices with spring cloud stream](https://piotrminkowski.com/2020/06/05/introduction-to-event-driven-microservices-with-spring-cloud-stream/)
    - [RabbitMQ docker-compose with default properties](https://github.com/changhuixu/rabbitmq-labs/tree/master/02_QueueProperties)
    - [Stackoverflow - Set Routing Key for Producer](https://stackoverflow.com/questions/52329361/spring-cloud-stream-reactive-how-to-set-routing-key-for-producer)
    - [Stackoverflow - Set multiple routing key for Consumer](https://stackoverflow.com/questions/50587227/multiple-bindingroutingkeys-for-a-consumer-with-spring-cloud-stream-using-rabbi)
- MinIO
    - [Deploy MinIO on Kubernetes](https://docs.min.io/docs/deploy-minio-on-docker-compose.html)
- Nginx
    - [Nginx as reverse proxy in front of keycloak](https://itnext.io/nginx-as-reverse-proxy-in-front-of-keycloak-21e4b3f8ec53)
    - [Keycloak with Java and ReactJS](https://www.powerupcloud.com/keycloak-with-java-and-reactjs/)
- Security
    - [Keycloak for Identity and Access Management & High Availability Deployment with Kubernetes](https://medium.com/devops-dudes/keycloak-for-identity-and-access-management-9860a994bf0)
    - [Baeldung - Spring Boot + Keycloak](https://www.baeldung.com/spring-boot-keycloak)
    - [Securing Spring Boot Rest APIs with Keycloak](https://medium.com/devops-dudes/securing-spring-boot-rest-apis-with-keycloak-1d760b2004e)
    - [Secure Frontend (React) and Backend (Node JS Express Rest API) with Keycloak](https://medium.com/devops-dudes/secure-front-end-react-js-and-back-end-node-js-express-rest-api-with-keycloak-daf159f0a94e)
    - [Role Based Access Control for multiple keycloak clients](https://janikvonrotz.ch/2020/04/30/role-based-access-control-for-multiple-keycloak-clients/)
    - [Spring Security + Keycloak](https://www.thomasvitale.com/spring-security-keycloak/)
    - [Keycloak authentication flow SSO Client](https://www.thomasvitale.com/keycloak-authentication-flow-sso-client/)
    - [Using Spring Boot OAuth2 instead of Keycloak adapters](https://medium.com/@bcarunmail/securing-rest-api-using-keycloak-and-spring-oauth2-6ddf3a1efcc2)
    - [Using Spring Boot OAuth2 instead of Keycloak adapters](https://wstutorial.com/rest/spring-security-oauth2-keycloak.html)
    - [Stackoverflow - Why do Bearer-only clients exist?](https://stackoverflow.com/questions/58911507/keycloak-bearer-only-clients-why-do-they-exist)
    - [Keycloak RestTemplate with Spring Boot Security Integration](https://ramonak.io/posts/vaadin%E2%80%93keycloak%E2%80%93spring-security-integration)
- WebSocket
    - [Websockets with react & express](https://dev.to/ksankar/websockets-with-react-express-part-2-4n9f)
    - [React Managing Websockets with redux and context](https://rossbulat.medium.com/react-managing-websockets-with-redux-and-context-61f9a06c125b)
    - [Baeldung - Spring Security Websockets](https://www.baeldung.com/spring-security-websockets)
    - [Stackoverflow - Spring Websocket authentication with spring security and keycloak](https://stackoverflow.com/questions/50573461/spring-websockets-authentication-with-spring-security-and-keycloak)
        - [Spring web socket Keycloak](https://github.com/stakater-lab/spring-web-socket-keycloak/blob/master/application/src/main/java/io/aurora/socketapp/config/WebSocketConfig.java)
        - [Stackoverflow - json web token jwt with Spring based sockjs stomp websocket](https://stackoverflow.com/questions/30887788/json-web-token-jwt-with-spring-based-sockjs-stomp-web-socket/39456274#39456274)
    - [dzone.com - Build a secure app using spring boot and websocket](https://dzone.com/articles/build-a-secure-app-using-spring-boot-and-websocket)
    - [okta - java spring websocket secure](https://developer.okta.com/blog/2019/10/09/java-spring-websocket-tutorial)
    - [Stomp Spring Boot Websocket](https://www.toptal.com/java/stomp-spring-boot-websocket)
    