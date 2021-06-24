import { Moment } from "moment";

export interface StorageInfo {
    filenames: string;
    numOfDownloadsLeft: number;
    expiryDatetime: Moment;
    storageId: string;
    allowAnonymousDownload: boolean;
}
