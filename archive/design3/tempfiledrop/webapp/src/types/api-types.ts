import { StorageInfo } from "storage-js-client";

export interface UserUploadInfo {
    id: string;
    user: string;
    storageInfo: StorageInfo;
}
