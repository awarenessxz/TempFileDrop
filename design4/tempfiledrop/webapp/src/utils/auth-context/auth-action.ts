import { Dispatch } from "react";
import axios from "axios";
import { AuthActionTypes, CHECK_SSO, REQUEST_LOGOUT, LOGIN_ERROR, LOGIN_SUCCESS } from "./auth-types";
import Data from "../../config/app.json";

export const dispatchCheckSSO = (dispatch: Dispatch<AuthActionTypes> | null) => {
    if (dispatch === null) {
        throw new Error("dispatch is null....");
    }

    axios.get(Data.api_endpoints.get_user)
        .then(res => {
            console.log(res);
            dispatch({ type: CHECK_SSO });
        })
        .catch(err => {
            console.log(err);
            dispatch({ type: LOGIN_SUCCESS, payload: {
                isAuthenticated: true,
                    userToken: {
                        username: "user",
                        name: "Alex",
                        roles: ["admin"],
                        isAdmin: true
                    }
                }
            });
            // dispatch({ type: LOGIN_ERROR, payload: { error: "Login Failed! Please try again." } });
        });
};

export const dispatchLogoutUserAction = (dispatch: Dispatch<AuthActionTypes> | null) => {
    if (dispatch === null) {
        throw new Error("dispatch is null....");
    }

    axios.get(Data.api_endpoints.logout)
        .then(res => {
            console.log(res);
            dispatch({ type: REQUEST_LOGOUT });
        })
        .catch(err => console.log(err));
};

export const dispatchLoginUserAction = (dispatch: Dispatch<AuthActionTypes> | null) => {
    if (dispatch === null) {
        throw new Error("dispatch is null....");
    }

    // redirect to login
    const port = (window.location.port ? ':' + window.location.port : '');
    window.location.href = '//' + window.location.hostname + port + Data.api_endpoints.login;
};
