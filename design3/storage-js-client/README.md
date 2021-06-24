# Storage-JS-Client

storage-js-client is a pure Javascript client written in Typescript implementing upload/download/delete logic for web 
application. The underlying http client used is **axios**.

## Usage

### Add the library dependencies.

```bash
yarn add storage-js-client axios moment
```

### Examples

Below are examples on how to use some of the functions available.

#### Upload

Below is an example of a normal upload. If you require an anonymous upload, use the `uploadAnonymously` function. 

```javascript
import { AxiosResponse } from "axios";
import StorageClient, { FileUploadMetadata } from "storage-js-client";

const uploadFunction = () => {
    const metadata: FileUploadMetadata = {
        bucket: "BUCKET_NAME",
        storagePath: "STORAGE_PATH",
        maxDownloads: 1,
        expiryPeriod: 1,
        allowAnonymousDownload: false,
        eventRoutingKey: "ROUTING KEY",
        eventData: JSON.stringify({ username: "USERNAME" })
    };
    StorageClient.upload({
        url: "/api/storagesvc/upload",
        files: acceptedFiles,
        metadata: metadata,
        headers: {
            'Authorization': 'Bearer ' + token
        },
        onUploadPercentage: (percentage) => console.log(percentage),
        onError: (err: any) => console.log(err),
        onResponse: (res: AxiosResponse) = console.log(res),
    });
};
```

#### Download

##### 2 Steps Download

This will get a download link from the storage service. Note that this download link have an expiry time. 

```javascript
StorageClient.getDownloadLink({
    url: "/api/storagesvc/download/temporarykey/<STORAGE_ID",
    onResponse: (downloadInfo?: DownloadInfo) => {
        if (downloadInfo) {
            setDownloadInfo(downloadInfo)
        } else {
            console.log("Download Link is Not Available!");
        }
    },
    onError: (err) => {
        console.log(err);
    }
});
```

Download using temporary link. The download endpoint is embedded in the `DownloadInfo`. Note that there are two types of
download link.
1. Secured --> Requires Authorization Header to download
2. Non Secured --> Allow downloads without any authorization

```javascript
StorageClient.download({
    url: downloadInfo.downloadEndpoint,
    eventRoutingKey: "OPTIONAL Routing Key",
    eventData: "OPTIONAL Event Data",
    headers: downloadInfo.requiresAuthentication ? { 'Authorization': 'Bearer ' + token } : {},
    onError(err: any): void {
        console.log(err);
    },
    onSuccess(): void {
        console.log("You have downloaded the files!");
    }
});
```

##### Direct Download

If you prefer to download directly, use the codes below. Do note that authentication is required.

```javascript
StorageClient.download({
    url: "/api/storagesvc/download/secure/<BUCKET_NAME>/<STORAGE_ID>",
    eventRoutingKey: "OPTIONAL Routing Key",
    eventData: "OPTIONAL Event Data",
    headers: { 
        'Authorization': 'Bearer ' + token 
    },
    onError(err: any): void {
        console.log(err);
    },
    onSuccess(): void {
        console.log("You have downloaded the files!");
    }
});
```

#### Delete

```javascript
StorageClient.deleteStorageId({
    url: "/api/storagesvc/<BUCKET_NAME>/<STORAGE_ID>",
    headers: { 'Authorization': 'Bearer ' + token },
    eventRoutingKey: "OPTIONAL Routing Key",
    eventData: "OPTIONAL Event Data",
    onSuccess: () => {
        console.log("Storage Deleted!");
    },
    onError: (err) => {
        console.error(err)
    }
});
```


