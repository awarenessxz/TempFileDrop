import qs from "qs";
import axios  from "axios";
import { FileMap, RequestParams, StringMap } from "../common";
import { BaseUploadParams } from "./base";

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

export interface S3PresignedUploadParams extends Omit<BaseUploadParams, 'files'|'onUploadPercentage'> {
    metadata: StorageS3PresignedUrlParams;
    files: FileMap;
    mode?: "POST" | "PUT";
    onUploadPercentage?: (objectName: string, percentage: number) => void;
}

/********************************************************************************************************
 * Functions
 ********************************************************************************************************/

export const uploadViaPresignedUrl =({
    files,
    metadata,
    onSuccess = (res: any) => {},
    onError = (err: any) => {},
    onUploadPercentage = (objectName: string, percentage: number) => {},
    headers = {},
    url = "/api/storagesvc/s3-upload-url",
    mode = "POST"
}: S3PresignedUploadParams) => {
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
    axios.get(url, {
        headers: headers,
        params: reqParams,
        paramsSerializer: (params) => qs.stringify(params, { arrayFormat: "repeat" })
    })
        .then(res => {
            const allRequestPromise = [];
            const presignedResponse = res.data as StorageS3PresignedUrlResponse;
            if (mode === "POST") {
                const formData = new FormData();
                for (let objectName in files) {
                    if (presignedResponse.s3PresignedPosts.hasOwnProperty(objectName)) {
                        // set form data
                        const storageFormData = presignedResponse.s3PresignedPosts[objectName]
                        for (let key in storageFormData) {
                            formData.append(key, storageFormData[key]);
                        }
                        formData.append("file", files[objectName]);
                        // set options
                        const options = {
                            onUploadProgress: (progressEvent: any) => {
                                const {loaded, total} = progressEvent;
                                const percent = Math.floor((loaded * 100) / total);
                                if (percent <= 100) {
                                    onUploadPercentage(objectName, percent);
                                }
                            },
                            headers: headers
                        };
                        // add request
                        allRequestPromise.push(axios.post(presignedResponse.s3Endpoint, formData, options));
                    } else {
                        throw Error("Presigned Url for " + objectName + " not found! Ensure keys in parameter 'files' and 'metadata.storageObjects' matches!");
                    }
                }
            } else {
                for (let objectName in files) {
                    if (presignedResponse.s3PresignedUrls.hasOwnProperty(objectName)) {
                        const url = presignedResponse.s3PresignedUrls[objectName]
                        const file = files[objectName]
                        // set options
                        const options = {
                            onUploadProgress: (progressEvent: any) => {
                                const {loaded, total} = progressEvent;
                                const percent = Math.floor((loaded * 100) / total);
                                if (percent <= 100) {
                                    onUploadPercentage(objectName, percent);
                                }
                            },
                            headers: headers
                        };
                        allRequestPromise.push(axios.put(url, file, options));
                    } else {
                        throw Error("Presigned Url for " + objectName + " not found! Ensure keys in parameter 'files' and 'metadata.storageObjects' matches!");
                    }
                }
            }

            return Promise.all(allRequestPromise)
        })
        .then(res => {
            onSuccess(res);
        })
        .catch(err =>{
            onError(err);
        });
};
