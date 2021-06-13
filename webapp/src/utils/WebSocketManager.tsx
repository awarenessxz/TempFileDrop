import React, { useEffect } from "react";
import { useDispatch } from "react-redux";
import { Message } from "@stomp/stompjs";
import {useAuthState} from "./auth-context";
import useStompWebSocket from "./hooks/UseStompWebSocket";
import { setOnFilesDownloadedMessageAction, setOnFilesDeletedMessageAction, setOnFilesUploadedMessageAction } from "../redux";
import Data from "../config/app.json";

const WebSocketManager: React.FC = ({ children }) => {
    const { isAuthenticated } = useAuthState();
    const { stompClient ,isWebSocketConnected } = useStompWebSocket({
        socketPath: Data.websocket_stomp_endpoint,
        authenticationToken: isAuthenticated ? window.accessToken : null,
        requiresAuth: true,
    });
    const dispatch = useDispatch();

    // connect websocket
    useEffect(() => {
        if (isWebSocketConnected && stompClient !== null) {
            stompClient.subscribe(Data.websocket_endpoints.on_files_deleted, (message: Message) => {
                dispatch(setOnFilesDownloadedMessageAction(message));
            });
            stompClient.subscribe(Data.websocket_endpoints.on_files_downloaded, (message: Message) => {
                dispatch(setOnFilesDeletedMessageAction(message));
            });
            stompClient.subscribe(Data.websocket_endpoints.on_files_uploaded, (message: Message) => {
                dispatch(setOnFilesUploadedMessageAction(message));
            });
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [isWebSocketConnected, stompClient]);

    return (
        <React.Fragment>
            { children }
        </React.Fragment>
    )
};

export default WebSocketManager;