# Storage Service

Storage Service that provides REST API Endpoints for **uploading, downloading and listing** files stored in either 
**file storage (Folders)** or **object storage (MinIO)**

- [Running the service (locally)](#running-the-service-locally)
- [Deploy the service](#deploying-the-service)
- [Uploading files to services (Mixed Multipart Request)](#uploading-files-to-the-service-mixed-multipart---multipart-request-with-json-data)

## Usage

### Running the service (Locally)

```bash
cd <ROOT>
./gradlew storage-service:bootRun
```

### Deploying the service


## Additional Info

### Identifying Storage Path

When using the service, the storage url is declared using the following format:

```bash
s3://<bucket_name>/<storage_path>

where
<bucket_name> is the bucket which you have registered and authorized to
<storage_path> is the path to the directory/file inside the bucket
```

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
