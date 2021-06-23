import React from "react";
import { RouteProps } from "react-router-dom";

const Dashboard = React.lazy(() => import("../components/Dashboard"));
const BucketConsole = React.lazy(() => import("../components/BucketConsole"));

const routes: RouteProps[] = [
    {
        exact: true,
        component: Dashboard,
        path: "/"
    },
    {
        exact: true,
        component: BucketConsole,
        path: "/buckets"
    }
];

export default routes;
