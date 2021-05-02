import React from 'react';
import { BrowserRouter as Router, Switch, Route } from "react-router-dom";
import NavBar from "./NavBar";
import PageNotFound from "../PageNotFound";
import routes from "../../config/routes";
import './App.css';

function App() {
  return (
    <div id="page-top">
        <Router>
            <NavBar />
            <div className="app-main">
                <React.Suspense fallback="Loading...">
                    <Switch>
                        {routes.map((r, idx) => <Route key={idx} exact={r.exact} path={r.path} component={r.component} /> )}
                        <Route path="*" component={PageNotFound}/>
                    </Switch>
                </React.Suspense>
            </div>
        </Router>
    </div>
  );
}

export default App;
