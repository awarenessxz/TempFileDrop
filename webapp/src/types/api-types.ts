import { Moment } from "moment";

export interface UserInfoResponse {
    userExists: boolean;
    userToken: String; // username
}

export interface FileUploadRequest {
    username: string;
    maxDownloads: number;
    expiryPeriod: number;
}

export interface FileUploadResponse {
    message: string;
    storageId: string;
}

export interface UploadedFiles {
    id: string;
    user: string;
    filenames: string;
    numOfDownloadsLeft: number;
    expiryDatetime: Moment;
    storageId: string;
    downloadLink: string;
}
