import { Message } from "@stomp/stompjs";

/* ***************************************************************************************
 * Types Definition for State
 *************************************************************************************** */

// redux state for websocket, add more message if you have more....
export interface WebSocketState {
    onFilesDownloadedMessage: Message | null;
    onFilesDeletedMessage: Message | null;
    onFilesUploadedMessage: Message | null;
}

/* ***************************************************************************************
 * List of all action type
 *************************************************************************************** */

export const UPDATE_ON_FILES_DELETED_MESSAGE = "UPDATE_ON_FILES_DELETED_MESSAGE";
export const UPDATE_ON_FILES_DOWNLOADED_MESSAGE = "UPDATE_ON_FILES_DOWNLOADED_MESSAGE";
export const UPDATE_ON_FILES_UPLOADED_MESSAGE = "UPDATE_ON_FILES_UPLOADED_MESSAGE";

/* ***************************************************************************************
 * Types Definition for all action type
 *************************************************************************************** */

interface SetOnFilesDeletedMessage {
    type: typeof UPDATE_ON_FILES_DELETED_MESSAGE;
    message: Message
}

interface SetOnFilesDownloadedMessage {
    type: typeof UPDATE_ON_FILES_DOWNLOADED_MESSAGE;
    message: Message
}

interface SetOnFilesUploadedMessage {
    type: typeof UPDATE_ON_FILES_UPLOADED_MESSAGE;
    message: Message
}

// union action types
export type WebSocketActions = SetOnFilesDownloadedMessage | SetOnFilesDeletedMessage | SetOnFilesUploadedMessage;
