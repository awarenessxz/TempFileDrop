export interface StorageFileDropzoneProps {
    /** Show the files dropped into react-dropzone in a table view */
    showDrops?: boolean;
    /** Show upload settings (Only when isAnonymousUpload = false) */
    showConfigs?: boolean;
    /** Maximum file upload for react-dropzone in bytes */
    maxSizeInBytes?: number;
    /** Set Anonymous Upload to Storage Service API Endpoint */
    isAnonymousUpload?: boolean;
    /** Upload Metadata for Uploading to Storage Service API Endpoint (Only when isAnonymousUpload = false ) */
    uploadMetadata?: UploadMetadata | undefined;
    /** Upload Success Callback */
    onSuccessfulUploadCallback?: () => void;
    /** Upload Error Callback */
    onErrorCallback?: (err: any) => void;
}

export interface UploadMetadata {
    /** Bucket to upload files to */
    bucket: string;
    /** Path to store file in within the bucket */
    storagePath: string;
    /** RabbitMQ Routing Key for publishing uploaded events */
    eventRoutingKey: string;
    /** Uploaded Events Data (OPTIONAL) */
    eventData?: string;
}

export interface FileUploadStatus {
    [key: string]: number;
}
