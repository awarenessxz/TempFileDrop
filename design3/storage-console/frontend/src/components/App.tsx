import React, { useState } from 'react';
import { BrowserRouter as Router, Switch, Route } from "react-router-dom";
import { createStyles, makeStyles, Theme } from '@material-ui/core/styles';
import CssBaseline from '@material-ui/core/CssBaseline';
import NavBar from "./NavBar";
import SideBar from "./SideBar";
import PageNotAuthorized from "./PageNotAuthorized";
import PageNotFound from "./PageNotFound";
import routes from "../config/routes";
import { useAuthState } from "../util/auth-context";

const useStyles = makeStyles((theme: Theme) =>
    createStyles({
        root: {
            display: 'flex',
        },
        toolbar: {
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'flex-end',
            padding: theme.spacing(0, 1),
            // necessary for content to be below app bar
            ...theme.mixins.toolbar,
        },
        content: {
            flexGrow: 1,
            padding: theme.spacing(3),
        },
    }),
);

const App = () => {
    const classes = useStyles();
    const [open, setOpen] = useState(false);
    const { isAuthenticated, authErrorMsg } = useAuthState();

    if (!isAuthenticated) {
        if (authErrorMsg) {
            return <PageNotAuthorized />;
        }
        return <div>Loading....</div>;
    }

    return (
        <div className={classes.root}>
            <CssBaseline />
            <Router>
                <NavBar isOpen={open} setIsOpen={(newState) => setOpen(newState)} />
                <SideBar isOpen={open} setIsOpen={(newState) => setOpen(newState)} />
                <main className={classes.content}>
                    <div className={classes.toolbar} />
                    <React.Suspense fallback="Loading...">
                        <Switch>
                            { routes.map((r, idx) => <Route key={idx} exact={r.exact} path={r.path} component={r.component} /> )}
                            <Route path="*" component={PageNotFound}/>
                        </Switch>
                    </React.Suspense>
                </main>
            </Router>
        </div>
    );
};

export default App;
