import { useEffect, useRef, useState } from 'react';
import { Client, Frame, StompConfig } from '@stomp/stompjs';

interface UseStompWebSocketProps {
    socketPath: string
    authenticationToken?: string | null,
    requiresAuth?: boolean,
    reconnectDelay?: number,
}

interface UseStompWebSocketState {
    stompClient: Client | null,
    isWebSocketConnected: boolean
}

const useStompWebSocket = ({
    socketPath,
    authenticationToken = null,
    reconnectDelay = 100000000,
    requiresAuth = false,
}: UseStompWebSocketProps): UseStompWebSocketState => {
    const ws = useRef<Client | null>(null);
    const [isConnected, setIsConnected] = useState(false);

    // get broker url -- https://github.com/stomp-js/ng2-stompjs/issues/129
    const getBrokerUrl = (path: string): string => {
        const currentPath = `${window.location.origin + window.location.pathname}`; // eg. http://localhost:8080/ws
        const url = new URL(path, currentPath);
        // convert protocol http -> ws and https -> wss
        if (url.protocol === "https:") {
            url.protocol = url.protocol.replace('https', 'wss');
        } else {
            url.protocol = url.protocol.replace('http', 'ws');
        }
        console.log(url.href);
        return url.href;
    };

    useEffect(() => {
        // deactivate before creating new connection
        if (ws.current !== null) {
            ws.current.deactivate().then(() => console.log("Disconnect old WebSocket connection..."));
        }
        if (requiresAuth && authenticationToken === null) {
            console.log("Waiting for authentication token...");
            return;
        }

        // connect web socket
        const stompConfig: StompConfig = {
            brokerURL: getBrokerUrl(socketPath),
            debug: (str: string): void => {
                console.debug(str);
            },
            onConnect: (frame: Frame): void => {
                console.log('WebSocket is connected!');
                setIsConnected(true);
            },
            onStompError: (frame: Frame): void => {
                console.log(`Broker reported error: ${frame.headers.message}`);
                console.log(`Additional Details: ${frame.body}`);
            },
            onWebSocketClose: (): void => {
                console.log("WebSocket Connection is closed");
            },
            reconnectDelay: reconnectDelay,
            heartbeatIncoming: 4000,
            heartbeatOutgoing: 4000,
            connectHeaders: requiresAuth ? { "Authorization": "Bearer " + authenticationToken } : {},
        };
        const stompClient = new Client(stompConfig);
        stompClient.activate();
        ws.current = stompClient;

        return (): void => {
            stompClient.deactivate().then(() => console.log("Disconnecting WebSocket..."));
            setIsConnected(false);
            ws.current = null;
        };
    }, [requiresAuth, authenticationToken, socketPath, reconnectDelay]);

    return { stompClient: ws.current, isWebSocketConnected: isConnected };
};

export default useStompWebSocket;