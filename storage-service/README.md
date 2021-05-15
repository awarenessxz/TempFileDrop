# Storage Service

Storage Service that provides REST API Endpoints for **uploading, downloading and listing** files stored in either 
**file storage (Folders)** or **object storage (MinIO)**

- [Usage](#usage)
    - [Configuring application.yaml](#configuring-applicationyaml)
    - [Running the service (locally)](#running-the-service-locally)
    - [Deploy the service](#deploying-the-service)
- [Documentation](#documentation)
    - [Storage Service Design](#storage-service-design)
    - [Database Tables](#database-tables)
    - [Uploading files to services (Mixed Multipart Request)](#uploading-files-to-the-service-mixed-multipart---multipart-request-with-json-data)

## Usage

### Configuring application.yaml

| Property Group | Property | Remarks |
| --- | --- | --- |
| storagesvc | storage-mode | Choice of storage mode = file / object |
| storagesvc.file-storage | upload-path | directory to upload files to |
| storagesvc.object-storage | minio-endpoint | endpoint of minio cluster |
| storagesvc.object-storage | minio-access-key | access key for minio cluster |
| storagesvc.object-storage | minio-access-secret | access secret for minio cluster |

### Running the service (Locally)

```bash
cd <ROOT>
./gradlew storage-service:bootRun
```

### Deploying the service

TO BE ADDED...

## Documentation

### Storage Service Design

#### How uploads are stored and tagged

![upload_design](../doc/storagesvc_design.png)

- Each upload is tag to a **storageId** which is stored in the database
- Following S3 convention, the consumer should have a bucket available in order to upload files

#### Expiry and downloads capped

As this is a temporary storage service, all uploads are tag with **expiry datetime** and **maximum number of downloads**. A 
scheduled job will run every day to clean up files that have already expired. 

### API Endpoints

| Request Type | Endpoints | Description |
| --- | --- | --- |
| GET | /storagesvc/{bucket} | get contents inside bucket |
| POST | /storagesvc/upload | upload files using multi-part request |
| DELETE | /storagesvc/{bucket}/{storageId} | delete files from storage using bucket name and storageId |
| GET | /storagesvc/storageinfo/{bucket}/{storageId} | get storage information using bucket name and storageId |
| POST | /storagesvc/storageinfo/bulk | get multiple storage information using bucket name and multiple storageId |
| GET | /storagesvc/download/{bucket}/{storageId} | download files using bucket name and storageId |

### Database Tables

| Table | Columns | Description |
| --- | --- | --- |
| storage_info | **id**, bucketName, storagePath,  filenames, numOfDownloadsLeft, expiryDatetime, downloadLink | {storageId to files/object uploads} mapping. Given the **id** AKA **storageId**, the storage service will be able to identify the files uploaded and make them available for download. |
| storage_files | id, bucketName, storagePath, storageFilename, storageFileContentType, storageFileLength, storageId | Contains the uploaded files details |

### Uploading files to the service (Mixed Multipart -> Multipart Request with JSON data)

Refer to the API documentation for details on Rest endpoints exposed by the Storage Service. To send multipart request 
with json data, use the following into your code as examples.

#### Using Javascript (React + Axios)

```javascript
const uploadfunction = (uploadedFiles, userInfo) => {
    const formData = new FormData();
    uploadedFiles.forEach(file => {
        formData.append("files", file);
    });
    const metadata = {
        username: userInfo.username
    }
    formData.append("metadata", new Blob([JSON.stringify(metadata)], {
        type: "application/json"
    }));

    // send request
    axios.post("/files/upload", formData, {}) {
        ...
    }       
};
```

#### Using Kotlin (Spring MVC with RestTemplate)

```kotlin
data class StorageInfo(
        val bucket: String,
        val folder: String
)
```

```kotlin
@PostMapping("/upload", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
fun uploadFile(
        @RequestPart("files", required = true) files: List<MultipartFile>,
        @RequestPart("metadata", required = true) metadata: StorageInfo
): ResponseEntity<FileStorageResponse> {
    // craft header
    val headers = HttpHeaders()
    headers.contentType = MediaType.MULTIPART_FORM_DATA
    
    // craft body
    val body: MultiValueMap<String, Any> = LinkedMultiValueMap()
    files.forEach { body.add("files", it.resource) }
    body.add("metadata", metadata)
    
    // craft request
    val requestEntity: HttpEntity<MultiValueMap<String, Any>> = HttpEntity(body, headers)
    
    // craft rest template
    val storageServiceUrl = "http://storage.service.com/upload"
    val restTemplate = RestTemplate()
    val response = restTemplate.postForEntity(storageServiceUrl, requestEntity, String::class.java)
}
```
