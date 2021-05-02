import React from "react";
import { Redirect, Route, RouteProps } from "react-router-dom";
import { useAuthState } from "./auth-context";

const ProtectedRoute = (props: RouteProps): JSX.Element => {
    const { userInfo } = useAuthState();
    const Component = props.component as React.ComponentType<any>;

    if (props.render) {
        return <Route {...props} />;
    }

    return (
        <Route
            exact={props.exact}
            path={props.path}
            render={(componentProps): JSX.Element => {
                const isAuthed = userInfo !== null;
                return isAuthed ? <Component {...componentProps} /> : <Redirect to="/notfound" />;
            }}
        />
    );
};

export default ProtectedRoute;
