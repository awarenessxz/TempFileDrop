import qs from "qs";
import axios from "axios";
import { Moment } from "moment";
import { RequestParams } from "../common";

/********************************************************************************************************
 * Type Definitions
 ********************************************************************************************************/

export interface StorageMetadata {
    id: string;
    bucket: string;
    objectName: string;
    fileContentType: string;
    fileSize: number;
    numOfDownloadsLeft: number;
    expiryDatetime: Moment;
    allowAnonymousDownload: boolean;
}

interface StorageMetadataMap {
    [key: string]: StorageMetadata;
}

export interface StorageMetadataResponse {
    storageMetadataList: StorageMetadataMap;
    expiredObjects: string[];
}

export interface StorageMetadataParams {
    onSuccess: (res: StorageMetadataResponse) => void;
    onError?: (err: any) => void;
    headers?: any;
    url?: string;
    storageObjects: string[];
}

/********************************************************************************************************
 * Functions
 ********************************************************************************************************/

export const getStorageMetadata = ({
    storageObjects,
    onSuccess,
    onError = (err: any) => {},
    headers = {},
    url = "/api/storagesvc/storageinfo"
}: StorageMetadataParams) => {
    const reqParams: RequestParams = { };
    reqParams["storageObjects"] = storageObjects;

    axios.get(url, {
        headers: headers,
        params: reqParams,
        paramsSerializer: (params) => qs.stringify(params, { arrayFormat: "repeat" })
    })
        .then(res => {
            if (res.status === 200) {
                onSuccess(res.data as StorageMetadataResponse);
            } else {
                onError("Storage Metadata not found!!");
            }
        })
        .catch(err => onError(err));
};
