import React from "react";
import Container from "react-bootstrap/cjs/Container";
import Col from "react-bootstrap/cjs/Col";
import Form from "react-bootstrap/cjs/Form";
import Row from "react-bootstrap/cjs/Row";

const Login = () => {
    return (
        <div className="content">
            <Container>
                <Form>
                    <Form.Group as={Row} controlId="formPlaintextEmail">
                        <Form.Label column sm="2">
                            Email
                        </Form.Label>
                        <Col sm="10">
                            <Form.Control plaintext defaultValue="email@example.com" />
                        </Col>
                    </Form.Group>

                    <Form.Group as={Row} controlId="formPlaintextPassword">
                        <Form.Label column sm="2">
                            Password
                        </Form.Label>
                        <Col sm="10">
                            <Form.Control type="password" placeholder="Password" />
                        </Col>
                    </Form.Group>
                </Form>
            </Container>
        </div>
    )
};

export default Login;
