import axios from "axios";
import { Moment } from "moment";
import { extractFilenameFromContentDisposition } from "../utils/file-utils";

export interface DownloadInfo {
    downloadEndpoint: string;
    expiryDatetime: Moment;
    tokenExpiryDateTime: Moment;
    numOfDownloadsLeft: number;
    requiresAuthentication: boolean;
}

export interface GetDownloadLinkParams {
    url: string;
    onSuccess: (downloadInfo: DownloadInfo) => void;
    onError?: (err: any) => void;
    headers?: any;
}

export const getDownloadLink = (params: GetDownloadLinkParams) => {
    axios.get(params.url, { headers: params.headers })
        .then(res => {
            if (res.status === 200) {
                const info: DownloadInfo = res.data;
                params.onSuccess(info);
            } else {
                if (params.onError) {
                    params.onError({
                        message: "Fail to retrieve download link"
                    });
                }
            }
        })
        .catch(err => {
            if (params.onError) {
                params.onError(err);
            }
        });
};

export interface DownloadParams {
    url: string;
    eventRoutingKey?: string;
    eventData?: string;
    onSuccess?: () => void;
    onError?: (err: any) => void;
    headers?: any;
}

interface DownloadRequestParams {
    [key: string]: string;
}

export const download = (params: DownloadParams) => {
    const reqParams: DownloadRequestParams = { };
    if (params.eventData) {
        reqParams["eventData"] = params.eventData;
    }
    if (params.eventRoutingKey) {
        reqParams["eventRoutingKey"] = params.eventRoutingKey
    }

    axios.get(params.url, {
        responseType: "blob",
        headers: params.headers,
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
            if (params.onSuccess) {
                params.onSuccess();
            }
        })
        .catch(err => {
            if (params.onError) {
                params.onError(err);
            }
        });
};
