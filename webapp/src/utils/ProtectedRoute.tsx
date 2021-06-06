import React from "react";
import { Redirect, Route, RouteProps } from "react-router-dom";
import { useAuthState } from "./auth-context";

const ProtectedRoute = (props: RouteProps): JSX.Element => {
    const { isAuthenticated } = useAuthState();
    const Component = props.component as React.ComponentType<any>;

    if (props.render) {
        return <Route {...props} />;
    }

    return (
        <Route
            exact={props.exact}
            path={props.path}
            render={(componentProps): JSX.Element => {
                return isAuthenticated ? <Component {...componentProps} /> : <Redirect to="/notfound" />;
            }}
        />
    );
};

export default ProtectedRoute;
