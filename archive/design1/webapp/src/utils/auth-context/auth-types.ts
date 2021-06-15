export interface UserInfo {
    username: string;
}

/* ***************************************************************************************
 * Type Definition of State
 *************************************************************************************** */

export interface AuthState {
    userInfo: UserInfo | null;
    loading: boolean;
    errorMsg: string | null;
}

/* ***************************************************************************************
 * List of all action type
 *************************************************************************************** */

export const REQUEST_LOGIN = "REQUEST_LOGIN";
export const REQUEST_LOGOUT = "REQUEST_LOGOUT";
export const LOGIN_SUCCESS = "LOGIN_SUCCESS";
export const LOGIN_ERROR = "LOGIN_ERROR"

/* ***************************************************************************************
 * Types Definition for all action type
 *************************************************************************************** */

interface LoginUserAction {
    type: typeof REQUEST_LOGIN;
    payload: {
        username: string;
        password: string;
    }
}

interface LogoutUserAction {
    type: typeof REQUEST_LOGOUT;
}

interface LoginUserSuccessAction {
    type: typeof LOGIN_SUCCESS;
    payload: {
        username: string;
    }
}

interface LoginUserErrorAction {
    type: typeof LOGIN_ERROR;
    payload: {
        error: string;
    }
}

// union action types
export type AuthActionTypes =
    | LoginUserAction
    | LogoutUserAction
    | LoginUserSuccessAction
    | LoginUserErrorAction;