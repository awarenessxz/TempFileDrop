import axios  from "axios";
import { BaseUploadParams } from "./base";

/********************************************************************************************************
 * Type Definitions
 ********************************************************************************************************/

export interface FileUploadMetadata {
    bucket: string;
    storagePrefix: string;
    maxDownloads: number;
    expiryPeriod: number;
    allowAnonymousDownload: boolean;
}

export interface FileUploadResponse {
    message: string;
    storageObjectList: string[];
}

interface FinalUploadParams {
    onSuccess: (uploadRes: FileUploadResponse) => void;
    onError: (err: any) => void;
    headers: any;
    url: string;
    onUploadPercentage: (percentage: number) => void;
    formData: FormData;
}

export interface UploadParams extends BaseUploadParams {
    metadata: FileUploadMetadata;
}

export type AnonymousUploadParams = BaseUploadParams;

interface FinalUploadParams {
    onSuccess: (uploadRes: FileUploadResponse) => void;
    onError: (err: any) => void;
    headers: any;
    url: string;
    onUploadPercentage: (percentage: number) => void;
    formData: FormData;
}

/********************************************************************************************************
 * Functions
 ********************************************************************************************************/

const uploadToStorageService = (params: FinalUploadParams) => {
    const options = {
        onUploadProgress: (progressEvent: any) => {
            const {loaded, total} = progressEvent;
            const percent = Math.floor((loaded * 100) / total);
            if (percent <= 100) {
                params.onUploadPercentage(percent);
            }
        },
        headers: params.headers
    };

    // send request
    axios.post(params.url, params.formData, options)
        .then(res => {
            if (res.status === 200) {
                const uploadResponse: FileUploadResponse = res.data;
                params.onSuccess(uploadResponse);
            } else {
                params.onError({ message: "Fail to upload file" });
            }
        })
        .catch(err => params.onError(err));
};

export const uploadAnonymously = ({
    files,
    onSuccess = (uploadRes: FileUploadResponse) => {},
    onError = (err: any) => {},
    headers = {},
    onUploadPercentage = (percentage: number) => {},
    url = "/api/storagesvc/anonymous/upload"
}: AnonymousUploadParams) => {
    const formData = new FormData();
    files.forEach(file => formData.append("files", file));
    return uploadToStorageService({
        onSuccess: onSuccess,
        onError: onError,
        headers: headers,
        onUploadPercentage: onUploadPercentage,
        url: url,
        formData: formData
    });
};

export const upload = ({
    files,
    metadata,
    onSuccess = (uploadRes: FileUploadResponse) => {},
    onError = (err: any) => {},
    headers = {},
    onUploadPercentage = (percentage: number) => {},
    url = "/api/storagesvc/upload"
}: UploadParams) => {
    const formData = new FormData();
    formData.append("metadata", new Blob([JSON.stringify(metadata)], {
        type: "application/json"
    }));
    files.forEach(file => formData.append("files", file));
    return uploadToStorageService({
        onSuccess: onSuccess,
        onError: onError,
        headers: headers,
        onUploadPercentage: onUploadPercentage,
        url: url,
        formData: formData
    });
};
