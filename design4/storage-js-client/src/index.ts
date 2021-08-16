import { FileMap, RequestParams } from "./common";
import { FileUploadResponse } from "./upload/base";
import { upload, uploadAnonymously, FileUploadMetadata, AnonymousUploadParams, UploadParams } from "./upload/proxy";
import { uploadViaPresignedUrl, StorageS3PresignedUrlParams, S3PresignedUploadParams, StorageS3PresignedUrlResponse } from "./upload/presignedurl";
import { downloadFileByStorageId, downloadFileByStoragePath, DownloadByStorageIdParams, DownloadByStoragePathParams } from "./download";
import { deleteFileByStorageId, deleteFileByStoragePath, DeleteStorageIdParams, DeleteStoragePathParams } from "./delete";
import { getStorageInfoByStoragePath, getStorageInfoByStorageId, StorageInfo, StorageInfoByStorageIdParams, StorageInfoByStoragePathParams } from "./storage";
import { extractFilenameFromContentDisposition } from "./utils/file-utils";

const StorageClient = {
    downloadFileByStorageId,
    downloadFileByStoragePath,
    upload,
    uploadAnonymously,
    uploadViaPresignedUrl,
    extractFilenameFromContentDisposition,
    deleteFileByStorageId,
    deleteFileByStoragePath,
    getStorageInfoByStoragePath,
    getStorageInfoByStorageId
};

export {
    FileMap,
    RequestParams,
    FileUploadMetadata,
    FileUploadResponse,
    AnonymousUploadParams,
    UploadParams,
    DownloadByStorageIdParams,
    DownloadByStoragePathParams,
    DeleteStorageIdParams,
    DeleteStoragePathParams,
    StorageInfo,
    StorageInfoByStorageIdParams,
    StorageInfoByStoragePathParams,
    S3PresignedUploadParams,
    StorageS3PresignedUrlParams,
    StorageS3PresignedUrlResponse
};

export default StorageClient;
