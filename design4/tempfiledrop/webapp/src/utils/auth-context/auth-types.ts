import { UserToken } from "../keycloak-utils";

/* ***************************************************************************************
 * Type Definition of State
 *************************************************************************************** */

export interface AuthState {
    isAuthReady: boolean;
    isAuthenticated: boolean;
    userToken: UserToken | null;
    authErrorMsg: string | null;
}

/* ***************************************************************************************
 * List of all action type
 *************************************************************************************** */

export const CHECK_SSO = "CHECK_SSO";
export const REQUEST_LOGOUT = "REQUEST_LOGOUT";
export const REQUEST_LOGIN = "REQUEST_LOGIN";
export const LOGIN_SUCCESS = "LOGIN_SUCCESS";
export const LOGIN_ERROR = "LOGIN_ERROR";

/* ***************************************************************************************
 * Types Definition for all action type
 *************************************************************************************** */

interface CheckSSOAction {
    type: typeof CHECK_SSO;
}

interface LoginUserAction {
    type: typeof REQUEST_LOGIN;
}

interface LoginUserSuccessAction {
    type: typeof LOGIN_SUCCESS;
    payload: {
        isAuthenticated: boolean;
        userToken: UserToken | null;
    }
}

interface LoginUserErrorAction {
    type: typeof LOGIN_ERROR;
    payload: {
        error: string;
    }
}

interface LogoutUserAction {
    type: typeof REQUEST_LOGOUT;
}

// union action types
export type AuthActionTypes =
    | CheckSSOAction
    | LoginUserAction
    | LoginUserSuccessAction
    | LoginUserErrorAction
    | LogoutUserAction;