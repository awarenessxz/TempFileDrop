export interface BaseUploadParams {
    files: File[];
    onSuccess?: (res: any) => void;
    onError?: (err: any) => void;
    headers?: any;
    url?: string;
    onUploadPercentage?: (percentage: number) => void;
}
