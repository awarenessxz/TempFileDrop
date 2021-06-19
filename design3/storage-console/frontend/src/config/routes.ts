import React from "react";
import { RouteProps } from "react-router-dom";

const Dashboard = React.lazy(() => import("../components/Dashboard"));
const ServiceConsole = React.lazy(() => import("../components/ServiceConsole"));
const BucketConsole = React.lazy(() => import("../components/BucketConsole"));

const routes: RouteProps[] = [
    {
        exact: true,
        component: Dashboard,
        path: "/"
    },
    {
        exact: true,
        component: ServiceConsole,
        path: "/services"
    },
    {
        exact: true,
        component: BucketConsole,
        path: "/buckets"
    },
];

export default routes;
