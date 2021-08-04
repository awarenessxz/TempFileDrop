import { Moment } from "moment";
import axios from "axios";

export interface StorageInfo {
    id: string;
    bucket: string;
    storagePath: string;
    originalFilename: string;
    fileContentType: string;
    fileLength: number;
    numOfDownloadsLeft: number;
    expiryDatetime: Moment;
    allowAnonymousDownload: boolean;
    storageFullPath: string;
}

interface StorageInfoRequestParams {
    [key: string]: string;
}

interface StorageInfoParams {
    onSuccess: (storageInfo: StorageInfo) => void;
    onError?: (err: any) => void;
    headers?: any;
    url?: string;
}

export interface StorageInfoByStorageIdParams extends StorageInfoParams{
    storageId: string;
}

export interface StorageInfoByStoragePathParams extends StorageInfoParams{
    storagePath: string;
}

export const getStorageInfoByStorageId = ({
    storageId,
    onSuccess,
    onError = (err: any) => {},
    headers = {},
    url = "/api/storagesvc/"
}: StorageInfoByStorageIdParams) => {
    const reqParams: StorageInfoRequestParams = { };
    reqParams["storageId"] = storageId;

    axios.get(url, { headers: headers, params: reqParams })
        .then(res => {
            if (res.status === 200) {
                onSuccess(res.data);
            } else {
                onError("Storage Info not found!!");
            }
        })
        .catch(err => onError(err));
};

export const getStorageInfoByStoragePath = ({
    storagePath,
    onSuccess,
    onError = (err: any) => {},
    headers = {},
    url = "/api/storagesvc/"
}: StorageInfoByStoragePathParams) => {
    const reqParams: StorageInfoRequestParams = { };
    reqParams["storagePath"] = storagePath;

    axios.get(url, { headers: headers, params: reqParams })
        .then(res => {
            if (res.status === 200) {
                onSuccess(res.data);
            } else {
                onError("Storage Info not found!!");
            }
        })
        .catch(err => onError(err));
};

