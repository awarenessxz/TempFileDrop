import { Moment } from "moment";

export interface UserInfoResponse {
    userExists: boolean;
    userToken: String; // username
}

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
    downloadLink: string;
}

export interface StorageInfo {
    filenames: string;
    numOfDownloadsLeft: number;
    expiryDatetime: Moment;
    storageId: string;
    downloadLink: string;
}

export interface UserUploadInfo {
    id: string;
    user: string;
    storageInfo: StorageInfo;
}
