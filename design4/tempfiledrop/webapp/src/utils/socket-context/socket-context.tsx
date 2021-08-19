import React, { createContext, useContext, useReducer, Dispatch } from "react";
import { SocketState, SocketActionTypes } from "./socket-types";
import { SocketReducer, initialState } from "./socket-reducer";

const SocketStateContext = createContext<SocketState|null>(null);
const SocketDispatchContext = createContext<Dispatch<SocketActionTypes>|null>(null);

export const useSocketState = (): SocketState | null => {
    const context = useContext(SocketStateContext);
    if (context === undefined) {
        throw new Error("useSocketState must be used within a SocketProvider");
    }
    return context;
};

export const useSocketDispatch = (): Dispatch<SocketActionTypes> | null => {
    const context = useContext(SocketDispatchContext);
    if (context === undefined) {
        throw new Error("useSocketDispatch must be used within a SocketProvider");
    }
    return context;
};

export const SocketProvider: React.FC = ({ children }) => {
    const [state, dispatch] = useReducer(SocketReducer, initialState);

    return (
        <SocketStateContext.Provider value={state}>
            <SocketDispatchContext.Provider value={dispatch}>
                { children }
            </SocketDispatchContext.Provider>
        </SocketStateContext.Provider>
    )
};
