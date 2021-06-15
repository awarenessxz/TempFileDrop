import axios from "axios";
import { Dispatch } from "react";
import { AuthActionTypes, LOGIN_ERROR, LOGIN_SUCCESS, REQUEST_LOGIN, REQUEST_LOGOUT } from "./auth-types";
import { UserInfoResponse } from "../../types/api-types";
import Data from "../../config/app.json";

export type LoginUserPayload = {
    username: string;
    password: string;
}

export const dispatchLoginUserAction = (dispatch: Dispatch<AuthActionTypes> | null, payload: LoginUserPayload) => {
    if (dispatch === null) {
        throw new Error("dispatch is null....");
    }

    // set loading = true
    dispatch({ type: REQUEST_LOGIN, payload });

    setTimeout(() => {
        // async processing (/user-info/login returns true/false)
        axios.post(Data.api_endpoints.mock_login, { ...payload})
            .then(res => {
                if (res.status === 200) {
                    const data: UserInfoResponse = res.data;
                    if (data.userExists) {
                        dispatch({ type: LOGIN_SUCCESS, payload: { username: payload.username } });
                    } else {
                        throw new Error("Login Failed")
                    }
                } else {
                    throw new Error("Login Failed");
                }
            })
            .catch(err => {
                // console.log(err);
                dispatch({ type: LOGIN_ERROR, payload: { error: "Login Failed! Please try again." } });
            })
    }, 500);
};

export const dispatchLogoutUserAction = (dispatch: Dispatch<AuthActionTypes> | null) => {
    if (dispatch === null) {
        throw new Error("dispatch is null....");
    }
    dispatch({ type: REQUEST_LOGOUT });
}
