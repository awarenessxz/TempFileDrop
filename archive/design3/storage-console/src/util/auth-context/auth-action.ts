import { Dispatch } from "react";
import {
    AuthActionTypes,
    INIT_KEYCLOAK,
    LOGIN_ERROR,
    LOGIN_SUCCESS,
    REQUEST_LOGIN
} from "./auth-types";
import Keycloak, { KeycloakInstance } from "keycloak-js";
import { extractUserToken } from "../keycloak-utils";

export const dispatchInitKeycloak = (dispatch: Dispatch<AuthActionTypes> | null) => {
    if (dispatch === null) {
        throw new Error("dispatch is null....");
    }

    const keycloak = Keycloak({
        url: process.env.REACT_APP_KEYCLOAK_AUTH_SERVER_URL,
        realm: process.env.REACT_APP_KEYCLOAK_REALM ? process.env.REACT_APP_KEYCLOAK_REALM : "",
        clientId: process.env.REACT_APP_KEYCLOAK_CLIENT_ID ? process.env.REACT_APP_KEYCLOAK_CLIENT_ID : ""
    });
    keycloak.init({ onLoad: "check-sso" })
        .then(authenticated => {
            if (authenticated) {
                window.accessToken = keycloak.token || "";
                dispatch({
                    type: INIT_KEYCLOAK,
                    payload: {
                        keycloak,
                        isAuthenticated: authenticated,
                        userToken: authenticated ? extractUserToken(keycloak.tokenParsed) : null,
                        errorMsg: authenticated ? null : "Login Failed! User is not registered!"
                    }
                });
            } else {
                dispatchLoginUserAction(dispatch, keycloak);
            }
        })
        .catch(error => {
            console.warn(error);
            dispatch({ type: INIT_KEYCLOAK, payload: { keycloak, isAuthenticated: false, userToken: null }});
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
            window.accessToken = keycloak.token || "";
            dispatch({
                type: LOGIN_SUCCESS,
                payload: {
                    isAuthenticated: authenticated,
                    userToken: extractUserToken(keycloak.tokenParsed)
                }
            });
        })
        .catch(error => {
            console.warn(error);
            dispatch({ type: LOGIN_ERROR, payload: { error: "Login Failed! Please try again." } });
        });
};
