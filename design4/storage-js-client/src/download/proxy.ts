import qs from "qs";
import axios from "axios";
import { RequestParams } from "../common";
import { BaseDownloadParams } from "./base";
import { downloadFile } from "../utils/download-utils";

/********************************************************************************************************
 * Type Definitions
 ********************************************************************************************************/

export interface DownloadParams extends BaseDownloadParams {
    storageObjects: string[];
}

/********************************************************************************************************
 * Functions
 ********************************************************************************************************/

export const downloadFromStorageService = ({
    storageObjects,
    onSuccess = () => {},
    onError = (err: any) => {},
    headers = {},
    url = "/api/storagesvc/download"
}: DownloadParams) => {
    const reqParams: RequestParams = { };
    reqParams["storageObjects"] = storageObjects;

    axios.get(url, {
        responseType: "blob",
        headers: headers,
        params: reqParams,
        paramsSerializer: (params) => qs.stringify(params, { arrayFormat: "repeat" })
    })
        .then(res => {
            downloadFile(res);
            onSuccess();
        })
        .catch(err => onError(err));
};
