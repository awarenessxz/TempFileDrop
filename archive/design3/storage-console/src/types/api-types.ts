import { Moment } from "moment";

export interface StorageInfo {
    filenames: string;
    numOfDownloadsLeft: number;
    expiryDatetime: Moment;
    storageId: string;
    allowAnonymousDownload: boolean;
    bucket: string;
    storagePath: string;
}

export interface FileSystemNode {
    isFile: boolean;
    label: string;
    storageId?: string | null;
    storageSize: number;
    storageFullPath: string;
    storageBucket: string;
    storageDownloadLeft: number;
    storageExpiryDatetime?: Moment | null;
    children: FileSystemNode[];
}

export interface EventData {
    bucket: string;
    storageId: string;
    storageFiles: string;
    eventType: string;
    publishedDateTime: Moment;
}
