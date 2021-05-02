# TempFileDrop.io

TempFileDrop.io is a project that replicates [file.io](https://www.file.io/) which is a super simple file sharing application.
The purpose of this project is to understand the mechanics behind building an object storage service application. The following
tech stack will be applied:
- **React** - front facing website
- **Nginx** - Web server to serve website
- **MinIO** - object storage server
- **Spring Boot** - API web services

**Table of Content**
- [Architecture Design](#architecture-design)
- [Usage](#usage)
    - [Start MinIO Service](#start-minio-service)
    - [Start TempFileDrop.io Service](#start-tempfiledropio-service)
- [References](#references)
    - [Command Cheat Sheet](doc/CHEATSHEET.md)

## Architecture Design

![Architecture](doc/architecture.png)

### Design Considerations

1. How much space do we need for the MinIO Cluster
2. API Service
    - Batch Calls
    - Speed of transfer
    - Mode of transfer
    - How much load can it handles
3. File Upload Mechanism
    - **Option A) Direct File Upload**
        - use HTTP `Content-Type` header on request to set the proper content.
            ```
            PUT /profile/image HTTP/1.1
            Content-Type: image/jpeg
            Content-Length: 284
            
            raw image content...
            ```
        - This is a straightforward method that is recommended in most cases.
    - **Option B) Multipart HTTP request [SELECTED]**
        - useful to support `uploading of multiple files at once` as well as supporting `different metadata` (eg combination 
        of images and JSON) in the same request.
    - **Option C) Two-step: Metadata + Upload**
        - Submit meta-data first using `POST` method and return a `201 Created` with the location of where to upload the content.
        - Submit a `PUT` request to upload content.
4. Possible File Upload Vulnerabilities
    - **Server Side Request Forgery Vulnerability**
5. Possible File Upload Defence
    - Renaming file name -- harder for attacker to find their uploaded file

## Usage

### Start MinIO Service

### Start TempFileDrop.io Service

1. Start up the Database
    ```bash 
    cd database
    cd mongodata && sudo rm -rf * && cd ..     # OPTIONAL
    docker-compose up -d
    ```
2. Start the Web Server
    ```bash
    ./gradlew webserver:bootRun
    ```
3. Start the Web Application
    ```bash
    cd webapp
    yarn install
    yarn start
    ```

## References
- [Command Cheat Sheet](doc/CHEATSHEET.md)
- API Design
    - [API Design Guidance: File Upload](https://tyk.io/api-design-guidance-file-upload/)

