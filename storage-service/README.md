# Storage Service

Storage Service that provides REST API Endpoints for **uploading, downloading and listing** files stored in either 
**file storage (Folders)** or **object storage (MinIO)**

- [Usage](#usage)
    - [Configuring application.yaml](#configuring-applicationyaml)
    - [Running the service (locally)](#running-the-service-locally)
    - [Deploy the service](#deploying-the-service)
- [Documentation](#documentation)
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

### Database Tables

| Table | Columns | Description |
| --- | --- | --- |
| storage_info | id, bucketName, storagePath, storageFilename, storageFileContentType, storageFileLength, storageId | {storageId to files/object uploads} mapping. Given the **storageId**, the storage service will be able to identify the files uploaded and make them available for download. |

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
