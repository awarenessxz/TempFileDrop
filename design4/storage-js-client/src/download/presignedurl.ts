import axios  from "axios";
import { StorageS3PresignedUrlParams, StorageS3PresignedUrlResponse, getPresignedUrl } from "../common";
import { BaseDownloadParams } from "./base";
import { downloadFile } from "../utils/download-utils";

/********************************************************************************************************
 * Type Definitions
 ********************************************************************************************************/

export interface S3PresignedDownloadParams extends BaseDownloadParams {
    metadata: StorageS3PresignedUrlParams;
}

/********************************************************************************************************
 * Functions
 ********************************************************************************************************/

export const downloadViaPresignedUrl = ({
    metadata,
    onError = (err: any) => {},
    onSuccess = () => {},
    headers = {},
    url = "/api/storagesvc/s3-download-url"
}: S3PresignedDownloadParams) => {
    getPresignedUrl({ url: url, metadata: metadata, headers: headers})
        .then(res => {
            const allRequestPromise = [];
            const presignedResponse = res.data as StorageS3PresignedUrlResponse;
            for (let objectName in presignedResponse.s3PresignedUrls) {
                const url = presignedResponse.s3PresignedUrls[objectName];
                allRequestPromise.push(axios.get(url));
            }
            return Promise.all(allRequestPromise);
        })
        .then(resArr => {
            resArr.forEach(res => {
                downloadFile(res);
                onSuccess();
            });
        })
        .catch(err =>{
            onError(err);
        });
};
