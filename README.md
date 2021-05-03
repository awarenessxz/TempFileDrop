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

**Developement Plan**
1. Add logic to store uploads into Database
1. Add frontend logic to show dashboard 
1. Set up MinIO Cluster
1. Set up MinIO Service
1. Switch webserver to store in MinIO instead of file storage
1. Add advanced upload options
1. Add developer API page
1. Create a logo

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
    - **Option A - Direct File Upload**
        - use HTTP `Content-Type` header on request to set the proper content.
            ```
            PUT /profile/image HTTP/1.1
            Content-Type: image/jpeg
            Content-Length: 284
            
            raw image content...
            ```
        - This is a straightforward method that is recommended in most cases.
    - **Option B - Multipart HTTP request [SELECTED]**
        - useful to support `uploading of multiple files at once` as well as supporting `different metadata` (eg combination 
        of images and JSON) in the same request.
    - **Option C - Two-step: Metadata + Upload**
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
- Frontend
    - [How to add login Authentication to React Applications](https://www.digitalocean.com/community/tutorials/how-to-add-login-authentication-to-react-applications)
    - [React Login Authentication using useContext and useReducer](https://soshace.com/react-user-login-authentication-using-usecontext-and-usereducer/)
    - [React File Upload/Download Example with Spring Rest Api](https://bezkoder.com/react-file-upload-spring-boot/)
    - [React-Dropzone.js](https://react-dropzone.js.org/)
        - [Tutorial Example 1](https://www.digitalocean.com/community/tutorials/react-react-dropzone)
        - [Tutorial Example 2](https://www.newline.co/@dmitryrogozhny/how-to-drag-and-drop-files-in-react-applications-with-react-dropzone--c6732c93)
        - [CSS Tricks: Drag and drop for file uploading](https://css-tricks.com/drag-and-drop-file-uploading/)
- Backend
    - [Spring Boot Multipart File Upload to Folder](https://bezkoder.com/spring-boot-file-upload/)
    - [Spring Boot Uploading and Downloading file from MinIO object store](https://blogs.ashrithgn.com/spring-boot-uploading-and-downloading-file-from-minio-object-store/)
    - [File Upload with Spring MVC](https://www.baeldung.com/spring-file-upload)
    
    