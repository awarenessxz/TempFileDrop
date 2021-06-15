import { AuthActionTypes, AuthState, LOGIN_ERROR, LOGIN_SUCCESS, REQUEST_LOGIN, REQUEST_LOGOUT } from "./auth-types";

export const initialState: AuthState = {
    userInfo: null,
    loading: false,
    errorMsg: null
}

export const AuthReducer = (state: AuthState = initialState, action: AuthActionTypes) => {
    switch (action.type) {
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
                userInfo: { username: action.payload.username },
                errorMsg: null
            }
        case LOGIN_ERROR:
            return {
                ...state,
                loading: false,
                errorMsg: action.payload.error
            };
        case REQUEST_LOGOUT:
            return {
                ...state,
                userInfo: null,
            }
        default:
            return state;
    }
};