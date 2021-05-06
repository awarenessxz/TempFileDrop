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
    downloadLink: string;
}

export interface UserUploadRecordsResponse {
    id: string;
    folder: string;
    uploadedFiles: string;
    storageId: string;
    downloadLink: string;
}