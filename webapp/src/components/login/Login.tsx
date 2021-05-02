import React, { MouseEvent, useState, useEffect } from "react";
import { useHistory } from 'react-router-dom';
import Alert from "react-bootstrap/cjs/Alert";
import Button from "react-bootstrap/cjs/Button";
import Container from "react-bootstrap/cjs/Container";
import Form from "react-bootstrap/cjs/Form";
import Spinner from "../common/loading/Spinner";
import { LoginUserPayload } from "../../utils/auth-context/auth-action";
import { dispatchLoginUserAction, useAuthDispatch, useAuthState } from "../../utils/auth-context";
import "./Login.css";

const Login = () => {
    const history = useHistory();
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const dispatch = useAuthDispatch();
    const { loading, errorMsg, userInfo } = useAuthState();

    useEffect(() => {
        if (userInfo) {
            history.push("/dashboard")
        }
    }, [userInfo]);

    const handleLogin = (e: MouseEvent<HTMLButtonElement>): void => {
        e.preventDefault();
        const payload: LoginUserPayload = { username, password };
        dispatchLoginUserAction(dispatch, payload);
    };

    if (loading) {
        return <Spinner />;
    }

    return (
        <div className="content">
            <Container>
                {errorMsg && <Alert variant="danger">{ errorMsg }</Alert>}
                <Form>
                    <Form.Group controlId="formUserName">
                        <Form.Label>Username</Form.Label>
                        <Form.Control type="text" placeholder="Enter username" value={username} onChange={e => setUsername(e.target.value)} />
                    </Form.Group>

                    <Form.Group controlId="formPassword">
                        <Form.Label>Password</Form.Label>
                        <Form.Control type="password" placeholder="Password" value={password} onChange={e => setPassword(e.target.value)} />
                    </Form.Group>
                    <Button variant="primary" onClick={handleLogin}>
                        Login
                    </Button>
                </Form>
            </Container>
        </div>
    );
};

export default Login;
