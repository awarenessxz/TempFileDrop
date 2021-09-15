import qs from "qs";
import axios from "axios";
import { RequestParams } from "../common";

/********************************************************************************************************
 * Type Definitions
 ********************************************************************************************************/

export interface DeleteParams {
    onSuccess?: (res: any) => void;
    onError?: (err: any) => void;
    headers?: any;
    url?: string;
    storageObjects: string[];
    bucket: string;
}

/********************************************************************************************************
 * Functions
 ********************************************************************************************************/

export const deleteFromStorageService = ({
    bucket,
    storageObjects,
    onSuccess = (res: any) => {},
    onError = (err: any) => {},
    headers = {},
    url = "/api/storagesvc/"
}: DeleteParams) => {
    const reqParams: RequestParams = { };
    reqParams["storageObjects"] = storageObjects;

    axios.delete(`${url}/${bucket}`, {
        headers: headers,
        params: reqParams,
        paramsSerializer: (params) => qs.stringify(params, { arrayFormat: "repeat" })
    })
        .then(res => {
            if (res.status === 200) {
                onSuccess(res.data);
            } else {
                onError("Fail to delete files!");
            }
        })
        .catch(err => onError(err));
};
