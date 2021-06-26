import axios from "axios";

export interface DeleteStorageParams {
    url: string;
    onSuccess: () => void;
    eventRoutingKey?: string;
    eventData?: string;
    onError?: (err: any) => void;
    headers?: any;
}

interface DeleteRequestParams {
    [key: string]: string;
}

export const deleteStorageId = (params: DeleteStorageParams) => {
    const reqParams: DeleteRequestParams = { };
    if (params.eventData) {
        reqParams["eventData"] = params.eventData;
    }
    if (params.eventRoutingKey) {
        reqParams["eventRoutingKey"] = params.eventRoutingKey
    }

    axios.delete(params.url, {
        headers: params.headers,
        params: reqParams
    })
        .then(res => {
            if (res.status === 200) {
                params.onSuccess();
            } else {
                if (params.onError) {
                    params.onError({
                        message: "Fail to delete storage"
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
