import { FileMap, RequestParams, StorageS3PresignedUrlParams, StorageS3PresignedUrlResponse } from "./common";
import { getStorageMetadata, StorageMetadata, StorageMetadataParams, StorageMetadataResponse } from "./metadata";
import { uploadToStorageService, UploadParams, FileUploadMetadata } from "./upload/proxy";
import { uploadViaPresignedUrl, S3PresignedUploadParams } from "./upload/presignedurl";
import { downloadFromStorageService, DownloadParams } from "./download/proxy";
import { downloadViaPresignedUrl, S3PresignedDownloadParams } from "./download/presignedurl";
import { deleteFromStorageService, DeleteParams } from "./delete";

const StorageClient = {
    deleteFromStorageService,
    downloadViaPresignedUrl,
    downloadFromStorageService,
    getStorageMetadata,
    uploadViaPresignedUrl,
    uploadToStorageService
};

export {
    FileMap,
    UploadParams,
    RequestParams,
    DeleteParams,
    DownloadParams,
    FileUploadMetadata,
    StorageMetadata,
    StorageMetadataParams,
    StorageMetadataResponse,
    S3PresignedUploadParams,
    S3PresignedDownloadParams,
    StorageS3PresignedUrlParams,
    StorageS3PresignedUrlResponse
};

export default StorageClient;
