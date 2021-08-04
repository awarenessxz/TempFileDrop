import axios from "axios";
import { extractFilenameFromContentDisposition } from "../utils/file-utils";

interface DownloadRequestParams {
    [key: string]: string;
}

interface DownloadParams {
    onSuccess?: () => void;
    onError?: (err: any) => void;
    eventData?: string;
    headers?: any;
    url?: string;
}

export interface DownloadByStorageIdParams extends DownloadParams {
    storageId: string;
}

export interface DownloadByStoragePathParams extends DownloadParams {
    storagePath: string;
}

export const downloadFileByStorageId = ({
     storageId,
     onSuccess = () => {},
     onError = (err: any) => {},
     eventData = "",
     headers = {},
     url = "/api/storagesvc/download"
}: DownloadByStorageIdParams) => {
    const reqParams: DownloadRequestParams = { };
    reqParams["eventData"] = eventData;
    reqParams["storageId"] = storageId;

    axios.get(url, {
        responseType: "blob",
        headers: headers,
        params: reqParams
    })
        .then(res => {
            const filename = extractFilenameFromContentDisposition(res.headers);
            const url = window.URL.createObjectURL(new Blob([res.data]));
            const link = document.createElement('a');
            link.href = url;
            link.setAttribute('download', filename);
            document.body.appendChild(link);
            link.click();
            onSuccess();
        })
        .catch(err => onError(err));
};

export const downloadFileByStoragePath = ({
    storagePath,
    onSuccess = () => {},
    onError = (err: any) => {},
    eventData = "",
    headers = {},
    url = "/api/storagesvc/download"
}: DownloadByStoragePathParams) => {
    const reqParams: DownloadRequestParams = { };
    reqParams["eventData"] = eventData;
    reqParams["storagePath"] = storagePath;

    axios.get(url, {
        responseType: "blob",
        headers: headers,
        params: reqParams
    })
        .then(res => {
            const filename = extractFilenameFromContentDisposition(res.headers);
            const url = window.URL.createObjectURL(new Blob([res.data]));
            const link = document.createElement('a');
            link.href = url;
            link.setAttribute('download', filename);
            document.body.appendChild(link);
            link.click();
            onSuccess();
        })
        .catch(err => onError(err));
};