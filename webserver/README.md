# Web Server

This is the dedicated Backend web server for TempFileDrop.io built using Spring Boot Framework. The main tasks of this server is
to serve data, handle logins & registration and process file uploads/downloads.

## Usage

```bash
cd <ROOT>
./gradlew webserver:bootRun
```

## Testing File Uploads (API)

Ensure `mongoDB`, `minIO` and `storage-service` is up. To test uploading of files, use `Postman` and use the settings below

![Test Webserver Upload](../doc/postman_webserver_upload.png)
