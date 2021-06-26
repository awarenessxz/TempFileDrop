import React, { MouseEvent, useState } from "react";
import SwaggerUI from "swagger-ui-react";
import "swagger-ui-react/swagger-ui.css";
import Alert from "react-bootstrap/cjs/Alert";
import Button from "react-bootstrap/cjs/Button";
import Container from "react-bootstrap/cjs/Container";
import Col from "react-bootstrap/cjs/Col";
import Row from "react-bootstrap/cjs/Row";
import Tab from "react-bootstrap/cjs/Tab";
import Tabs from "react-bootstrap/cjs/Tabs";
import VerticallyCenteredModal from "../common/modal/VerticallyCenteredModal";
import DeveloperGuide from "./DeveloperGuide";
import { useAuthState } from "../../utils/auth-context";
import Data from "../../config/app.json";
import "./Developer.css";

const Developer = () => {
    const [key, setKey] = useState('readme');
    const [bearerToken, setBearerToken] = useState("");
    const [modalShow, setModalShow] = useState(false);
    const { isAuthenticated } = useAuthState();

    const generateBearerToken = (e: MouseEvent<HTMLButtonElement>) => {
        if (isAuthenticated) {
            setBearerToken(window.accessToken);
        } else {
            setBearerToken("");
        }
        setModalShow(true);
    };

    return (
        <Container className="developer-container">
            <Row className="header-container">
                <Col>
                    <h1>Developer's Guide to consume Centralized Storage Service</h1>
                </Col>
            </Row>
            <Row className="bearer-container">
                <Col>
                    <Button onClick={generateBearerToken}>Get Bearer Token</Button>
                    <VerticallyCenteredModal
                        title="Bearer Token"
                        content={bearerToken
                            ? <Alert variant="info" className="bearer-msg">{ bearerToken }</Alert>
                            : <Alert variant="danger">Please Login First!</Alert>
                        }
                        show={modalShow}
                        onHide={() => setModalShow(false)} />
                </Col>
            </Row>
            <Row className="tab-container">
                <Col>
                    <Tabs activeKey={key} onSelect={(k: any) => setKey(k)}>
                        <Tab eventKey="readme" title="Getting Started">
                            <DeveloperGuide />
                        </Tab>
                        <Tab eventKey="swagger" title="Swagger API">
                            <SwaggerUI url={Data.api_endpoints.storagesvc_swagger} />
                        </Tab>
                    </Tabs>
                </Col>
            </Row>
        </Container>
    );
};

export default Developer;
