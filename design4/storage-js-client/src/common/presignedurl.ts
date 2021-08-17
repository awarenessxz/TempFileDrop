import qs from "qs";
import axios  from "axios";
import {RequestParams, StringMap} from "./map";

/********************************************************************************************************
 * Type Definitions
 ********************************************************************************************************/

interface PresignedPost {
    [key: string]: StringMap;
}

type PresignedPut = StringMap;

export interface StorageS3PresignedUrlParams {
    bucket: string;
    storageObjects: string[];
    maxDownloads?: number;
    expiryPeriod?: number;
    allowAnonymousDownload?: boolean;
}

export interface StorageS3PresignedUrlResponse {
    s3PresignedUrls: PresignedPut;
    s3PresignedPosts: PresignedPost;
    s3Endpoint: string;
}

interface GetPresignedUrlParams {
    url: string;
    metadata: StorageS3PresignedUrlParams;
    headers?: any;
}

/********************************************************************************************************
 * Functions
 ********************************************************************************************************/

export const getPresignedUrl = ({
    url,
    metadata,
    headers = {}
}: GetPresignedUrlParams) => {
    // set params
    const reqParams: RequestParams = { };
    reqParams["bucket"] = metadata.bucket;
    reqParams["storageObjects"] = metadata.storageObjects;
    if (metadata.allowAnonymousDownload) {
        reqParams["allowAnonymousDownload"] = metadata.allowAnonymousDownload;
    }
    if (metadata.expiryPeriod) {
        reqParams["expiryPeriod"] = metadata.expiryPeriod;
    }
    if (metadata.maxDownloads) {
        reqParams["maxDownloads"] = metadata.maxDownloads;
    }

    // get presigned url
    return axios.get(url, {
        headers: headers,
        params: reqParams,
        paramsSerializer: (params) => qs.stringify(params, { arrayFormat: "repeat" })
    });
};
