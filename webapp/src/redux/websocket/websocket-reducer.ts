import {
    UPDATE_ON_FILES_DELETED_MESSAGE,
    UPDATE_ON_FILES_DOWNLOADED_MESSAGE,
    UPDATE_ON_FILES_UPLOADED_MESSAGE,
    WebSocketActions,
    WebSocketState
} from "./websocket-actions.types";

const initialState: WebSocketState = {
    onFilesDeletedMessage: null,
    onFilesDownloadedMessage: null,
    onFilesUploadedMessage: null,
};

const webSocketReducer = (state: WebSocketState = initialState, action: WebSocketActions): WebSocketState => {
    switch (action.type) {
        case UPDATE_ON_FILES_DELETED_MESSAGE:
            return {
                ...state,
                onFilesDeletedMessage: action.message,
            };
        case UPDATE_ON_FILES_DOWNLOADED_MESSAGE:
            return {
                ...state,
                onFilesDownloadedMessage: action.message,
            };
        case UPDATE_ON_FILES_UPLOADED_MESSAGE:
            return {
                ...state,
                onFilesUploadedMessage: action.message,
            };
        default:
            return state;
    }
};

export default webSocketReducer;
