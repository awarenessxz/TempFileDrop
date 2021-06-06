import {
    AuthActionTypes,
    AuthState,
    INIT_KEYCLOAK,
    LOGIN_ERROR,
    LOGIN_SUCCESS,
    REQUEST_LOGIN
} from "./auth-types";

export const initialState: AuthState = {
    loading: false,
    errorMsg: null,
    userToken: null,
    keycloak: null,
    isAuthenticated: false
}

export const AuthReducer = (state: AuthState = initialState, action: AuthActionTypes) => {
    switch (action.type) {
        case INIT_KEYCLOAK:
            return {
                ...state,
                keycloak: action.payload.keycloak,
                isAuthenticated: action.payload.isAuthenticated,
                userToken: action.payload.userToken
            };
        case REQUEST_LOGIN:
            return {
                ...state,
                loading: true,
                errorMsg: null
            };
        case LOGIN_SUCCESS:
            return {
                ...state,
                loading: false,
                errorMsg: null,
                isAuthenticated: action.payload.isAuthenticated,
                userToken: action.payload.userToken
            };
        case LOGIN_ERROR:
            return {
                ...state,
                loading: false,
                errorMsg: action.payload.error
            };
        default:
            return state;
    }
};