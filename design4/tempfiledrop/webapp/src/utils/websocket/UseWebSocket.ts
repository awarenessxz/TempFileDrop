import { useEffect, useRef, useState } from 'react';

interface UseWebsocketProps {
    socketPath: string,
    defaultRetry?: number,
    retryInterval?: number
}

interface UseWebsocketState {
    ws: WebSocket | null,
    data: string;
    sendFn: () => void,
    isWebsocketConnected: boolean
}

const useWebSocket = ({
    socketPath,
    defaultRetry = 3,
    retryInterval = 1500
}: UseWebsocketProps): UseWebsocketState => {
    const ws = useRef<WebSocket|null>(null);
    const [data, setData] = useState("");   // message & timestamp
    const [sendFn, setSendFn] = useState(() => () => {}); // send function
    const [retry, setRetry] = useState(defaultRetry); // retry connection
    const [isConnected, setIsConnected] = useState(false);

    // get broker url -- https://github.com/stomp-js/ng2-stompjs/issues/129
    const getBrokerUrl = (path: string): string => {
        const currentPath = `${window.location.origin + window.location.pathname}`; // eg. http://localhost:8080/
        const url = new URL(path, currentPath);
        // convert protocol http -> ws and https -> wss
        if (url.protocol === "https:") {
            url.protocol = url.protocol.replace('https', 'wss');
        } else {
            url.protocol = url.protocol.replace('http', 'ws');
        }
        return url.href;
    };

    useEffect(() => {
        if (socketPath === "") {
            return;
        }

        const websocket = new WebSocket(getBrokerUrl(socketPath));
        websocket.onopen = () => {
            console.log("Websocket connection is opened");
            setIsConnected(true);
            setSendFn(() => {
                return (data: any) => {
                    try {
                        const message = JSON.stringify(data);
                        websocket.send(message);
                        return true;
                    } catch (err) {
                        return false;
                    }
                }
            });

            websocket.onmessage = event => {
                console.log("e", event);
            };
        };
        websocket.onclose = () => {
            console.log("Websocket connection is closed");
            setIsConnected(false);
            if (retry > 0) {
                setTimeout(() => {
                    setRetry((retry) => retry - 1);
                }, retryInterval);
            }
        };
        websocket.onerror = (err) => {
            console.error("Websocket encounted error: ", err);
            websocket.close();
        };
        ws.current = websocket;

        // unmount
        return (): void => {
            websocket.close();
        };
    }, [socketPath, retry, retryInterval]);

    return { ws: ws.current, data, sendFn, isWebsocketConnected: isConnected };
};

export default useWebSocket;