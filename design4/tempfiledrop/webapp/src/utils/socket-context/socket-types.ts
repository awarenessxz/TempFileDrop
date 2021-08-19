import { Message } from "@stomp/stompjs";

/* ***************************************************************************************
 * Type Definition of State
 *************************************************************************************** */

export interface SocketState {
    type: string;
    message: Message | null;
}

/* ***************************************************************************************
 * List of all action type
 *************************************************************************************** */

export const RECEIVED_MESSAGE = "RECEIVED_MESSAGE";

/* ***************************************************************************************
 * Types Definition for all action type
 *************************************************************************************** */

interface ReceivedMessageAction {
    type: typeof RECEIVED_MESSAGE;
    payload: {
        type: string;
        message: Message;
    }
}

// union action types
export type SocketActionTypes = ReceivedMessageAction