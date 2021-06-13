import { Message } from "@stomp/stompjs";
import {
    UPDATE_ON_FILES_DELETED_MESSAGE,
    UPDATE_ON_FILES_DOWNLOADED_MESSAGE,
    UPDATE_ON_FILES_UPLOADED_MESSAGE,
    WebSocketActions,
} from "./websocket-actions.types";

/* ***************************************************************************************
 * Action Creators (Standard Redux Actions)
 *************************************************************************************** */

export const setOnFilesDeletedMessageAction = (message: Message): WebSocketActions => ({
    type: UPDATE_ON_FILES_DELETED_MESSAGE,
    message
});

export const setOnFilesDownloadedMessageAction = (message: Message): WebSocketActions => ({
    type: UPDATE_ON_FILES_DOWNLOADED_MESSAGE,
    message
});

export const setOnFilesUploadedMessageAction = (message: Message): WebSocketActions => ({
    type: UPDATE_ON_FILES_UPLOADED_MESSAGE,
    message
});

