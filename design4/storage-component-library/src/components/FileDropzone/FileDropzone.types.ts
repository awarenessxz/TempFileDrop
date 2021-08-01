import { AxiosResponse } from "axios";

export interface FileDropzoneProps {
    /** Set the endpoint for file upload */
    uploadUrl: string;
    /** Show the files dropped into react-dropzone in a table view */
    showDrops?: boolean;
    /** Maximum file upload for react-dropzone in bytes */
    maxSizeInBytes?: number;
    /** Upload Success Callback */
    onSuccessfulUploadCallback?: (response: AxiosResponse) => void;
    /** Upload Error Callback */
    onErrorCallback?: (err: any) => void;
}
