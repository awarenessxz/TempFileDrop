import React, { MouseEvent } from "react";
import { Link, useHistory } from "react-router-dom";
import Container from "react-bootstrap/cjs/Container";
import Nav from "react-bootstrap/cjs/Nav";
import Navbar from "react-bootstrap/cjs/Navbar";
import { dispatchLogoutUserAction, useAuthDispatch, useAuthState } from "../../utils/auth-context";
import Data from "../../config/app.json";

interface MenuItem {
    path: string | undefined;
    label: string;
    login_required: boolean | undefined;
    hide_when_login: boolean | undefined;
}

const NavBar = () => {
    const history = useHistory();
    const { userInfo } = useAuthState();
    const dispatch = useAuthDispatch();
    const isLoggedIn = userInfo != null;

    const handleLogout = (e: MouseEvent) => {
        e.preventDefault();
        dispatchLogoutUserAction(dispatch);
        history.push("/");
    };

    const handleNoPath = (item: MenuItem, idx: number) => {
        if (item.label === "Logout") {
            return <Nav.Link key={idx} onClick={handleLogout}>{item.label}</Nav.Link>;
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
                                if (!isLoggedIn) {
                                    return null;
                                }
                            }
                            if (item.hide_when_login && isLoggedIn) {
                                return null;
                            }
                            if (item.path) {
                                return <Nav.Link key={idx} as={Link} to={item.path}>{item.label}</Nav.Link>;
                            }
                            // @ts-ignore
                            return handleNoPath(item, idx);
                        })}
                    </Nav>
                </Container>
            </Navbar>
        </div>
    );
};

export default NavBar;
