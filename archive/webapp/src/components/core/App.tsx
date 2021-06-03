import React from 'react';
import { BrowserRouter as Router, Switch, Route } from "react-router-dom";
import NavBar from "./NavBar";
import PageNotFound from "./PageNotFound";
import ProtectedRoute from "../../utils/ProtectedRoute";
import { AuthProvider } from "../../utils/auth-context";
import routes from "../../config/routes";
import './App.css';

function App() {
    return (
        <div id="page-top">
            <AuthProvider>
                <Router>
                    <NavBar />
                    <div className="app-main">
                        <React.Suspense fallback="Loading...">
                            <Switch>
                                {routes.map((r, idx) => {
                                    const { isPrivate, ...routerProps } = r;
                                    if (isPrivate) {
                                        return <ProtectedRoute key={idx} {...routerProps} />
                                    }
                                    return <Route key={idx} exact={r.exact} path={r.path} component={r.component} />
                                })}
                                <Route path="*" component={PageNotFound}/>
                            </Switch>
                        </React.Suspense>
                    </div>
                </Router>
            </AuthProvider>
        </div>
    );
}

export default App;
