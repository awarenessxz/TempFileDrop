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

export interface UploadParams extends BaseUploadParams {
    metadata?: FileUploadMetadata;
    isAnonymous?: boolean;
}

/********************************************************************************************************
 * Functions
 ********************************************************************************************************/

export const uploadToStorageService = ({
    files,
    metadata = undefined,
    onSuccess = (uploadRes: any) => {},
    onError = (err: any) => {},
    headers = {},
    onUploadPercentage = (percentage: number) => {},
    isAnonymous = false,
    url = isAnonymous ? "/api/storagesvc/anonymous/upload" : "/api/storagesvc/upload",
}: UploadParams) => {
    const formData = new FormData();
    if (!isAnonymous) {
        if (metadata === undefined) {
            throw new Error("Metadata is undefined!");
        }
        formData.append("metadata", new Blob([JSON.stringify(metadata)], {
            type: "application/json"
        }));
    }
    files.forEach(file => formData.append("files", file));

    const options = {
        onUploadProgress: (progressEvent: any) => {
            const {loaded, total} = progressEvent;
            const percent = Math.floor((loaded * 100) / total);
            if (percent <= 100) {
                onUploadPercentage(percent);
            }
        },
        headers: headers
    };

    // send request
    axios.post(url, formData, options)
        .then(res => {
            if (res.status === 200) {
                onSuccess(res.data);
            } else {
                onError({ message: "Fail to upload file" });
            }
        })
        .catch(err => onError(err));
};

