import { FileMap, RequestParams, StorageS3PresignedUrlParams, StorageS3PresignedUrlResponse } from "./common";
import { uploadToStorageService, UploadParams, FileUploadMetadata } from "./upload/proxy";
import { uploadViaPresignedUrl, S3PresignedUploadParams } from "./upload/presignedurl";
import { downloadFromStorageService, DownloadParams } from "./download/proxy";
import { downloadViaPresignedUrl, S3PresignedDownloadParams } from "./download/presignedurl";

import { deleteFileByStorageId, deleteFileByStoragePath, DeleteStorageIdParams, DeleteStoragePathParams } from "./delete";
import { getStorageInfoByStoragePath, getStorageInfoByStorageId, StorageInfo, StorageInfoByStorageIdParams, StorageInfoByStoragePathParams } from "./storage";

const StorageClient = {
    downloadViaPresignedUrl,
    downloadFromStorageService,
    uploadViaPresignedUrl,
    deleteFileByStorageId,
    deleteFileByStoragePath,
    getStorageInfoByStoragePath,
    getStorageInfoByStorageId,
    uploadToStorageService
};

export {
    FileMap,
    UploadParams,
    RequestParams,
    DownloadParams,
    FileUploadMetadata,
    DeleteStorageIdParams,
    DeleteStoragePathParams,
    StorageInfo,
    StorageInfoByStorageIdParams,
    StorageInfoByStoragePathParams,
    S3PresignedUploadParams,
    S3PresignedDownloadParams,
    StorageS3PresignedUrlParams,
    StorageS3PresignedUrlResponse
};

export default StorageClient;
