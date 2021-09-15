import { Dispatch } from "react";
import axios from "axios";
import { AuthActionTypes, CHECK_SSO } from "./auth-types";
import { UserToken } from "../../types/api-types";
import Data from "../../config/app.json";

export const dispatchCheckSSO = (dispatch: Dispatch<AuthActionTypes> | null) => {
    if (dispatch === null) {
        throw new Error("dispatch is null....");
    }

    axios.get(Data.api_endpoints.get_user)
        .then(res => {
            const user = res.data as UserToken;
            console.log(user);
            window.accessToken = user.token;
            const userToken = { ...user, isAdmin: user.roles.includes(Data.admin_role) }
            dispatch({ type: CHECK_SSO, payload: { isAuthenticated: true, userToken: userToken }});
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
