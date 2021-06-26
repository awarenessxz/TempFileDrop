import { upload, uploadAnonymously, FileUploadMetadata, FileUploadResponse, BaseUploadParams, UploadParams } from "./upload";
import { download, DownloadInfo, DownloadParams, getDownloadLink, GetDownloadLinkParams } from "./download";
import { deleteStorageId, DeleteStorageParams } from "./delete";
import { StorageInfo } from "./storage";
import { extractFilenameFromContentDisposition } from "./utils/file-utils";

const StorageClient = {
    download,
    getDownloadLink,
    upload,
    uploadAnonymously,
    extractFilenameFromContentDisposition,
    deleteStorageId
};

export {
    FileUploadMetadata,
    FileUploadResponse,
    BaseUploadParams,
    UploadParams,
    DownloadInfo,
    DownloadParams,
    GetDownloadLinkParams,
    DeleteStorageParams,
    StorageInfo
};

export default StorageClient;
