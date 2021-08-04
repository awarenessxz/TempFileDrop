import { upload, uploadAnonymously, FileUploadMetadata, FileUploadResponse, AnonymousUploadParams, UploadParams } from "./upload";
import { downloadFileByStorageId, downloadFileByStoragePath, DownloadByStorageIdParams, DownloadByStoragePathParams } from "./download";
import { deleteFileByStorageId, deleteFileByStoragePath, DeleteStorageIdParams, DeleteStoragePathParams } from "./delete";
import { getStorageInfoByStoragePath, getStorageInfoByStorageId, StorageInfo, StorageInfoByStorageIdParams, StorageInfoByStoragePathParams } from "./storage";
import { extractFilenameFromContentDisposition } from "./utils/file-utils";

const StorageClient = {
    downloadFileByStorageId,
    downloadFileByStoragePath,
    upload,
    uploadAnonymously,
    extractFilenameFromContentDisposition,
    deleteFileByStorageId,
    deleteFileByStoragePath,
    getStorageInfoByStoragePath,
    getStorageInfoByStorageId
};

export {
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
    StorageInfoByStoragePathParams
};

export default StorageClient;
