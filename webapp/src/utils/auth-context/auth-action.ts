import { Dispatch } from "react";
import {
    AuthActionTypes,
    CustomKeycloakTokenParsed,
    INIT_KEYCLOAK,
    LOGIN_ERROR,
    LOGIN_SUCCESS,
    REQUEST_LOGIN,
    UserToken
} from "./auth-types";
import Keycloak, { KeycloakInstance } from "keycloak-js";

const extractUserToken = (token: CustomKeycloakTokenParsed | undefined): UserToken | null => {
    if (token) {
        return {
            username: token.preferred_username,
            name: token.name,
            roles: token.roles,
            isAdmin: token.roles.includes("admin")
        };
    }
    return null;
};

export const dispatchInitKeycloak = (dispatch: Dispatch<AuthActionTypes> | null) => {
    if (dispatch === null) {
        throw new Error("dispatch is null....");
    }

    const keycloak = Keycloak("/keycloak.json") ;
    keycloak.init({ onLoad: "check-sso" })
        .then(authenticated => {
            window.accessToken = keycloak.token || "";
            dispatch({
                type: INIT_KEYCLOAK,
                payload: {
                    keycloak,
                    isAuthenticated: authenticated,
                    userToken: extractUserToken(keycloak.tokenParsed as CustomKeycloakTokenParsed)
                }
            });
        })
        .catch(() => {
            dispatch({ type: INIT_KEYCLOAK, payload: { keycloak: null, isAuthenticated: false, userToken: null }});
        });
};

export const dispatchLoginUserAction = (dispatch: Dispatch<AuthActionTypes> | null, keycloak: KeycloakInstance | null) => {
    if (dispatch === null) {
        throw new Error("dispatch is null....");
    }
    if (keycloak === null) {
        throw new Error("keycloak instance is null....");
    }

    // set loading = true
    dispatch({ type: REQUEST_LOGIN });

    // keycloak login
    keycloak.init({ onLoad: "login-required" })
        .then(authenticated => {
            dispatch({
                type: LOGIN_SUCCESS,
                payload: {
                    isAuthenticated: authenticated,
                    userToken: extractUserToken(keycloak.tokenParsed as CustomKeycloakTokenParsed)
                }
            });
        })
        .catch(error => {
            dispatch({ type: LOGIN_ERROR, payload: { error: "Login Failed! Please try again." } });
        });
};
