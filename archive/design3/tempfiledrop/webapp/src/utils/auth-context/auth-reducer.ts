import {
    AuthActionTypes,
    AuthState,
    INIT_KEYCLOAK,
    LOGIN_ERROR,
    LOGIN_SUCCESS,
    REQUEST_LOGIN
} from "./auth-types";

export const initialState: AuthState = {
    isAuthReady: false,
    authErrorMsg: null,
    userToken: null,
    keycloak: null,
    isAuthenticated: false
}

export const AuthReducer = (state: AuthState = initialState, action: AuthActionTypes) => {
    switch (action.type) {
        case INIT_KEYCLOAK:
            return {
                ...state,
                isAuthReady: true,
                keycloak: action.payload.keycloak,
                isAuthenticated: action.payload.isAuthenticated,
                userToken: action.payload.userToken
            };
        case REQUEST_LOGIN:
            return {
                ...state,
                isAuthReady: false,
                authErrorMsg: null
            };
        case LOGIN_SUCCESS:
            return {
                ...state,
                isAuthReady: true,
                authErrorMsg: null,
                isAuthenticated: action.payload.isAuthenticated,
                userToken: action.payload.userToken
            };
        case LOGIN_ERROR:
            return {
                ...state,
                isAuthReady: true,
                authErrorMsg: action.payload.error
            };
        default:
            return state;
    }
};