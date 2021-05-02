import React from "react";
import { RouteProps } from "react-router-dom";

const HomePage = React.lazy(() => import("../components/home/HomePage"));
const DeveloperPage = React.lazy(() => import("../components/developer/Developer"));
const DashboardPage = React.lazy(() => import("../components/dashboard/Dashboard"));
const LoginPage = React.lazy(() => import("../components/login/Login"));

const routes: RouteProps[] = [
    {
        exact: true,
        component: HomePage,
        path: "/"
    },
    {
        exact: true,
        component: DeveloperPage,
        path: "/developer"
    },
    {
        exact: true,
        component: LoginPage,
        path: "/login"
    },
    {
        exact: true,
        component: DashboardPage,
        path: "/dashboard"
    }
];

export default routes;
