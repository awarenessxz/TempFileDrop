import axios from "axios";

interface DeleteRequestParams {
    [key: string]: string;
}

interface DeleteStorageParams {
    onSuccess: () => void;
    onError?: (err: any) => void;
    eventData?: string;
    headers?: any;
    url?: string;
}

export interface DeleteStorageIdParams extends DeleteStorageParams {
    storageId: string;
}

export interface DeleteStoragePathParams extends DeleteStorageParams {
    storagePath: string;
}

export const deleteFileByStorageId = ({
    storageId,
    onSuccess,
    onError = (err: any) => {},
    eventData = "",
    headers = {},
    url = "/api/storagesvc/"
}: DeleteStorageIdParams) => {
    const reqParams: DeleteRequestParams = { };
    reqParams["eventData"] = eventData;
    reqParams["storageId"] = storageId;

    axios.delete(url, { headers: headers, params: reqParams})
        .then(res => {
            if (res.status === 200) {
                onSuccess();
            } else {
                onError({ message: "Fail to delete file using storageId - " + storageId });
            }
        })
        .catch(err => onError(err));
};

export const deleteFileByStoragePath = ({
    storagePath,
    onSuccess,
    onError = (err: any) => {},
    eventData = "",
    headers = {},
    url = "/api/storagesvc/"
}: DeleteStoragePathParams) => {
    const reqParams: DeleteRequestParams = { };
    reqParams["eventData"] = eventData;
    reqParams["storagePath"] = storagePath;

    axios.delete(url, { headers: headers, params: reqParams})
        .then(res => {
            if (res.status === 200) {
                onSuccess();
            } else {
                onError({ message: "Fail to delete file using storagePath - " + storagePath });
            }
        })
        .catch(err => onError(err));
};
