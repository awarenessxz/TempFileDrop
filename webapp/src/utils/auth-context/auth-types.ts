import { KeycloakInstance } from "keycloak-js";
import { UserToken } from "../keycloak-utils";

/* ***************************************************************************************
 * Type Definition of State
 *************************************************************************************** */

export interface AuthState {
    loading: boolean;
    errorMsg: string | null;
    userToken: UserToken | null;
    keycloak: KeycloakInstance | null;
    isAuthenticated: boolean;
}

/* ***************************************************************************************
 * List of all action type
 *************************************************************************************** */

export const INIT_KEYCLOAK = "INIT_KEYCLOAK";
export const REQUEST_LOGIN = "REQUEST_LOGIN";
export const LOGIN_SUCCESS = "LOGIN_SUCCESS";
export const LOGIN_ERROR = "LOGIN_ERROR";

/* ***************************************************************************************
 * Types Definition for all action type
 *************************************************************************************** */

interface InitKeycloakAction {
    type: typeof INIT_KEYCLOAK;
    payload: {
        keycloak: KeycloakInstance;
        isAuthenticated: boolean;
        userToken: UserToken | null;
        errorMsg?: string | null;
    }
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

// union action types
export type AuthActionTypes =
    | InitKeycloakAction
    | LoginUserAction
    | LoginUserSuccessAction
    | LoginUserErrorAction;