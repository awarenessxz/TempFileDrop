import { Dispatch } from "react";
import axios from "axios";
import { AuthActionTypes, REQUEST_LOGIN, REQUEST_LOGOUT } from "./auth-types";
import Data from "../../config/app.json";

export const dispatchCheckSSO = (dispatch: Dispatch<AuthActionTypes> | null) => {
    if (dispatch === null) {
        throw new Error("dispatch is null....");
    }

    axios.get(Data.api_endpoints.get_user)
        .then(res => console.log(res))
        .catch(err => console.log(err));
};

export const dispatchLogoutUserAction = (dispatch: Dispatch<AuthActionTypes> | null) => {
    if (dispatch === null) {
        throw new Error("dispatch is null....");
    }

    axios.get(Data.api_endpoints.logout)
        .then(res => dispatch({ type: REQUEST_LOGOUT }))
        .catch(err => console.log(err));
};

export const dispatchLoginUserAction = (dispatch: Dispatch<AuthActionTypes> | null) => {
    if (dispatch === null) {
        throw new Error("dispatch is null....");
    }

    // set loading = true
    dispatch({ type: REQUEST_LOGIN });

    // keycloak login
    //
    //
    //
    //
    // keycloak.init({ onLoad: "login-required" })
    //     .then(authenticated => {
    //         dispatch({
    //             type: LOGIN_SUCCESS,
    //             payload: {
    //                 isAuthenticated: authenticated,
    //                 userToken: extractUserToken(keycloak.tokenParsed)
    //             }
    //         });
    //     })
    //     .catch(error => {
    //         console.warn(error);
    //         dispatch({ type: LOGIN_ERROR, payload: { error: "Login Failed! Please try again." } });
    //     });
};
