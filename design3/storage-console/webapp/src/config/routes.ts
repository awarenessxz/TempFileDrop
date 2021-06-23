import React from "react";
import { RouteProps } from "react-router-dom";

const Dashboard = React.lazy(() => import("../components/Dashboard"));
const ServiceConsole = React.lazy(() => import("../components/ServiceConsole"));
const BucketConsole = React.lazy(() => import("../components/BucketConsole"));

export interface AppRouteProps extends RouteProps {
    isPrivate: boolean;
}

const routes: AppRouteProps[] = [
    {
        exact: true,
        component: Dashboard,
        path: "/",
        isPrivate: false
    },
    {
        exact: true,
        component: ServiceConsole,
        path: "/services",
        isPrivate: false
    },
    {
        exact: true,
        component: BucketConsole,
        path: "/buckets",
        isPrivate: false
    }
];

export default routes;
