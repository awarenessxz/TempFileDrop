import { Moment } from "moment";

export interface DownloadResponse {
    downloadEndpoint: string;
    expiryDatetime: Moment;
    tokenExpiryDateTime: Moment;
    numOfDownloadsLeft: number;
    requiresAuthentication: boolean;
}

export interface FileUploadMetadata {
    bucket: string;
    storagePath: string;
    maxDownloads: number;
    expiryPeriod: number;
    allowAnonymousDownload: boolean;
    eventRoutingKey: string;
    eventData?: string;
}

export interface FileUploadResponse {
    message: string;
    storageId: string;
}

export interface StorageInfo {
    filenames: string;
    numOfDownloadsLeft: number;
    expiryDatetime: Moment;
    storageId: string;
    allowAnonymousDownload: boolean;
}

export interface UserUploadInfo {
    id: string;
    user: string;
    storageInfo: StorageInfo;
}
