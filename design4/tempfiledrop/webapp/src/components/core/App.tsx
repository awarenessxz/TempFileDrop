import React, { useEffect, useState } from 'react';
import { BrowserRouter as Router, Switch, Route } from "react-router-dom";
import NavBar from "./NavBar";
import PageNotFound from "./PageNotFound";
import Spinner from "../common/loading/Spinner";
import VerticallyCenteredModal from "../common/modal/VerticallyCenteredModal";
import Alert from "react-bootstrap/cjs/Alert";
import ProtectedRoute from "../../utils/ProtectedRoute";
import { useAuthState } from "../../utils/auth-context";
import routes from "../../config/routes";
import './App.css';

function App() {
    const [modalShow, setModalShow] = useState(false);
    const { authErrorMsg, isAuthReady } = useAuthState();

    useEffect(() => {
        if (authErrorMsg) {
            setModalShow(true);
        }
    }, [authErrorMsg]);

    return (
        <div id="page-top">
            <Router basename="/tempfiledrop">
                <NavBar />
                {(!isAuthReady) && <Spinner isFullPage spinnerHeight={400} spinnerWidth={400} spinnerType="Puff" spinnerColor="#64a19d" backgroundColor="#000" />}
                {authErrorMsg && <VerticallyCenteredModal title="Login Error" content={<Alert variant="danger">{ authErrorMsg }</Alert>} show={modalShow} onHide={() => setModalShow(false)} />}
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
        </div>
    );
}

export default App;
