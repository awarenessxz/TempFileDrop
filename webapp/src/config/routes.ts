import React from "react";
import { RouteProps } from "react-router-dom";

const HomePage = React.lazy(() => import("../components/home/HomePage"));
const DeveloperPage = React.lazy(() => import("../components/developer/Developer"));
const DashboardPage = React.lazy(() => import("../components/dashboard/Dashboard"));

export interface AppRouteProps extends RouteProps {
    isPrivate: boolean;
}

const routes: AppRouteProps[] = [
    {
        exact: true,
        component: HomePage,
        path: "/",
        isPrivate: false
    },
    {
        exact: true,
        component: DeveloperPage,
        path: "/developer",
        isPrivate: false
    },
    {
        exact: true,
        component: DashboardPage,
        path: "/dashboard",
        isPrivate: false
    }
];

export default routes;
