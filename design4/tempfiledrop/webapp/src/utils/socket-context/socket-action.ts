import { Dispatch } from "react";
import { Message } from "@stomp/stompjs";
import { SocketActionTypes, RECEIVED_MESSAGE } from "./socket-types";

export const dispatchReceivedMessage = (dispatch: Dispatch<SocketActionTypes> | null, type: string, message: Message) => {
    if (dispatch === null) {
        throw new Error("dispatch is null....");
    }
    dispatch({ type:  RECEIVED_MESSAGE, payload: { type, message } });
};
