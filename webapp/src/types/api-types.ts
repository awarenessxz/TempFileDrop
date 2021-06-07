import { Moment } from "moment";

export interface FileUploadRequest {
    bucket: string;
    storagePath: string;
    maxDownloads: number;
    expiryPeriod: number;
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
}

export interface UserUploadInfo {
    id: string;
    user: string;
    storageInfo: StorageInfo;
}
