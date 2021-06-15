import React, { Dispatch } from "react";
import { AuthActionTypes, AuthState } from "./auth-types";
import { AuthReducer, initialState } from "./auth-reducer";

const AuthStateContext = React.createContext<AuthState | null>(null); // This context object will contain the authentication token and user details.
const AuthDispatchContext = React.createContext<Dispatch<AuthActionTypes> | null>(null); // We will use this context object to pass the dispatch method given to us by the useReducer that we will be creating later to manage the state. This makes it easy to provide the dispatch method to components that need it.

export const useAuthState = (): AuthState => {
    const context = React.useContext(AuthStateContext);
    if (context === undefined) {
        throw new Error("useAuthState must be used within a AuthProvider");
    }
    if (context === null) {
        return {
            ...initialState,
            errorMsg: "Error loading data...Please refresh page!"
        };
    }
    return context;
};

export const useAuthDispatch = (): Dispatch<AuthActionTypes> | null => {
    const context = React.useContext(AuthDispatchContext);
    if (context === undefined) {
        throw new Error("useAuthDispatch must be used within a AuthProvider");
    }
    return context;
};

export const AuthProvider: React.FC = ({ children }) => {
    const [state, dispatch] = React.useReducer(AuthReducer, initialState);

    return (
        <AuthStateContext.Provider value={state}>
            <AuthDispatchContext.Provider value={dispatch}>
                {children}
            </AuthDispatchContext.Provider>
        </AuthStateContext.Provider>
    );
};