import { RECEIVED_MESSAGE, SocketState, SocketActionTypes } from "./socket-types";

export const initialState: SocketState = {
    type: "",
    message: null
};

export const SocketReducer = (state: SocketState = initialState, action: SocketActionTypes) => {
    switch (action.type) {
        case RECEIVED_MESSAGE:
            return action.payload;
        default:
            return state;
    }
};
