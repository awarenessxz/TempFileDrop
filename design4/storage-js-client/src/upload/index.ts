import axios  from "axios";

export interface FileUploadMetadata {
    bucket: string;
    storagePath: string;
    maxDownloads: number;
    expiryPeriod: number;
    allowAnonymousDownload: boolean;
    eventRoutingKey: string;
    eventData?: string;
}

export interface FileUploadResponse {
    message: string;
    storageId: string;
}

export interface BaseUploadParams {
    url: string;
    files: File[];
    onSuccess: (uploadRes: FileUploadResponse) => void;
    onError?: (err: any) => void;
    headers?: any;
    onUploadPercentage?: (percentage: number) => void;
}

export interface UploadParams extends BaseUploadParams {
    metadata: FileUploadMetadata;
}

type UploadToStorageServiceParams = Omit<UploadParams, "metadata"|"files"> & { formData: FormData }

const uploadToStorageService = (params: UploadToStorageServiceParams) => {
    const options = {
        onUploadProgress: (progressEvent: any) => {
            const {loaded, total} = progressEvent;
            const percent = Math.floor((loaded * 100) / total);
            if (percent <= 100) {
                if (params.onUploadPercentage) {
                    params.onUploadPercentage(percent);
                }
            }
        },
        headers: params.headers
    };

    // send request
    axios.post(params.url, params.formData, options)
        .then(res => {
            if (res.status === 200) {
                const uploadResponse: FileUploadResponse = res.data;
                params.onSuccess(uploadResponse);
            } else {
                if (params.onError) {
                    params.onError({
                        message: "Fail to upload file"
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

export const upload = (params: UploadParams) => {
    const formData = new FormData();
    formData.append("metadata", new Blob([JSON.stringify(params.metadata)], {
        type: "application/json"
    }));
    params.files.forEach(file => formData.append("files", file));
    return uploadToStorageService({ ...params, formData});
};

export const uploadAnonymously = (params: BaseUploadParams) => {
    const formData = new FormData();
    params.files.forEach(file => formData.append("files", file));
    return uploadToStorageService({ ...params, formData});
};
