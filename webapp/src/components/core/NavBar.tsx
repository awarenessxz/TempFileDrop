import React, { MouseEvent, useEffect, useState } from "react";
import { Link, useHistory } from "react-router-dom";
import Container from "react-bootstrap/cjs/Container";
import Nav from "react-bootstrap/cjs/Nav";
import Navbar from "react-bootstrap/cjs/Navbar";
import Spinner from "../common/loading/Spinner";
import VerticallyCenteredModal from "../common/modal/VerticallyCenteredModal";
import { dispatchLoginUserAction, useAuthDispatch, useAuthState } from "../../utils/auth-context";
import Data from "../../config/app.json";
import Alert from "react-bootstrap/cjs/Alert";

interface MenuItem {
    path: string | undefined;
    label: string;
    login_required: boolean | undefined;
    hide_when_login: boolean | undefined;
}

const NavBar = () => {
    const history = useHistory();
    const [modalShow, setModalShow] = useState(false);
    const { loading, errorMsg, keycloak, isAuthenticated } = useAuthState();
    const dispatch = useAuthDispatch();

    useEffect(() => {
        if (errorMsg) {
            setModalShow(true);
        }
    }, [errorMsg]);

    const handleLogout = (e: MouseEvent) => {
        e.preventDefault();
        history.push("/");
        keycloak?.logout();
    };

    const handleLogin = (e: MouseEvent) => {
        e.preventDefault();
        dispatchLoginUserAction(dispatch, keycloak);
    };

    const handleNoPath = (item: MenuItem, idx: number) => {
        if (item.label === "Logout") {
            return <Nav.Link key={idx} onClick={handleLogout}>{item.label}</Nav.Link>;
        }
        if (item.label === "Login") {
            return <Nav.Link key={idx} onClick={handleLogin}>{item.label}</Nav.Link>;
        }
        throw new Error("Invalid Configuration in app.json");
    };

    return (
        <div className="app-header">
            <Navbar>
                <Container>
                    <Navbar.Brand as={Link} to="/">{ Data.web_name }</Navbar.Brand>
                    <Nav>
                        {Data.navbar_menu.map((item, idx) => {
                            if (item.login_required) {
                                if (!isAuthenticated) {
                                    return null;
                                }
                            }
                            if (item.hide_when_login && isAuthenticated) {
                                return null;
                            }
                            if (item.path) {
                                return <Nav.Link key={idx} as={Link} to={item.path}>{item.label}</Nav.Link>;
                            }
                            // @ts-ignore
                            return handleNoPath(item, idx); // for login & logout links
                        })}
                    </Nav>
                </Container>
            </Navbar>
            {loading && <Spinner isFullPage spinnerHeight={400} spinnerWidth={400} spinnerType="Puff" spinnerColor="#64a19d" backgroundColor="#000" />}
            {errorMsg && <VerticallyCenteredModal title="Login Error" content={<Alert variant="danger">{ errorMsg }</Alert>} show={modalShow} onHide={() => setModalShow(false)} />}
        </div>
    );
};

export default NavBar;
