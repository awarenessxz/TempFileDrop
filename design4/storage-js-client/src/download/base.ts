export interface BaseDownloadParams {
    onSuccess?: () => void;
    onError?: (err: any) => void;
    headers?: any;
    url?: string;
}