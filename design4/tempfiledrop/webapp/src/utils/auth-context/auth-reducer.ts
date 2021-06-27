import {
    AuthActionTypes,
    AuthState,
    CHECK_SSO,
    LOGIN_ERROR,
    LOGIN_SUCCESS,
    REQUEST_LOGIN,
    REQUEST_LOGOUT
} from "./auth-types";

export const initialState: AuthState = {
    isAuthReady: false,
    authErrorMsg: null,
    userToken: null,
    isAuthenticated: false
}

export const AuthReducer = (state: AuthState = initialState, action: AuthActionTypes) => {
    switch (action.type) {
        case CHECK_SSO:
            return {
                ...state,
                isAuthReady: true,
            };
        case REQUEST_LOGOUT:
            return {
                isAuthReady: true,
                authErrorMsg: null,
                userToken: null,
                isAuthenticated: false
            }
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
                isAuthenticated: false,
                authErrorMsg: action.payload.error
            };
        default:
            return state;
    }
};