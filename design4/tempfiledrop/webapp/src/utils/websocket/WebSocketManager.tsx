import React, { useEffect } from "react";
import { Message } from "@stomp/stompjs";
import useStompWebSocket from "./UseStompWebSocket";
import { useSocketDispatch } from "../socket-context";
import { dispatchReceivedMessage } from "../socket-context";
import { EventType, NotificationWebSocketTopics, NotificationWebSocketEndpoint } from "storage-js-client";

const WebSocketManager: React.FC = ({ children }) => {
    const { stompClient ,isWebSocketConnected } = useStompWebSocket({
        socketPath: NotificationWebSocketEndpoint
    });
    const dispatch = useSocketDispatch();

    // connect websocket
    useEffect(() => {
        if (isWebSocketConnected && stompClient !== null) {
            stompClient.subscribe(NotificationWebSocketTopics.FILES_DELETED, (message: Message) => {
                dispatchReceivedMessage(dispatch, EventType.FILES_DELETED, message);
            });
            stompClient.subscribe(NotificationWebSocketTopics.FILES_DOWNLOADED, (message: Message) => {
                dispatchReceivedMessage(dispatch, EventType.FILES_DOWNLOADED, message);
            });
            stompClient.subscribe(NotificationWebSocketTopics.FILES_UPLOADED, (message: Message) => {
                dispatchReceivedMessage(dispatch, EventType.FILES_UPLOADED, message);
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