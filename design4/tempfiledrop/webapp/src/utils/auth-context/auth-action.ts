import { Dispatch } from "react";
import axios from "axios";
import { AuthActionTypes, CHECK_SSO } from "./auth-types";
import Data from "../../config/app.json";
import { UserToken } from "../keycloak-utils";

export const dispatchCheckSSO = (dispatch: Dispatch<AuthActionTypes> | null) => {
    if (dispatch === null) {
        throw new Error("dispatch is null....");
    }

    axios.get(Data.api_endpoints.get_user)
        .then(res => {
            const user = res.data as UserToken;
            console.log("Check SSO --> ", user);
            window.accessToken = user.token;
            dispatch({ type: CHECK_SSO, payload: { isAuthenticated: true, userToken: { ...user, isAdmin: user.roles.includes("tempfiledrop|admin") }}});
        })
        .catch(err => {
            console.log(err);
            window.accessToken = "";
            dispatch({ type: CHECK_SSO, payload: { isAuthenticated: false, userToken: null } });
        });
};

export const dispatchLogoutUserAction = (dispatch: Dispatch<AuthActionTypes> | null) => {
    if (dispatch === null) {
        throw new Error("dispatch is null....");
    }

    // redirect to logout
    const port = (window.location.port ? ':' + window.location.port : '');
    window.location.href = '//' + window.location.hostname + port + Data.api_endpoints.logout;
};

export const dispatchLoginUserAction = (dispatch: Dispatch<AuthActionTypes> | null) => {
    if (dispatch === null) {
        throw new Error("dispatch is null....");
    }

    // redirect to login
    const port = (window.location.port ? ':' + window.location.port : '');
    window.location.href = '//' + window.location.hostname + port + Data.api_endpoints.login;
};
