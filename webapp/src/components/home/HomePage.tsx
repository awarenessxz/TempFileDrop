import React from "react";
import Button from "react-bootstrap/cjs/Button";
import Container from "react-bootstrap/cjs/Container";
import Col from "react-bootstrap/cjs/Col";
import Row from "react-bootstrap/cjs/Row";
import "./HomePage.css";

const HomePage = () => {
    return (
        <React.Fragment>
            <header className="masthead">
                <Container>
                    <div className="mx-auto text-center">
                        <h1>TempFileDrop.io</h1>
                        <h3>Convenient way to share your files</h3>
                        <Button variant="primary" size="lg">Upload</Button>
                    </div>
                </Container>
            </header>
            <section className="about">
                <Container>
                    <Row>
                        <Col>Section to describe how to use website</Col>
                    </Row>
                </Container>
            </section>
        </React.Fragment>
    );
};

export default HomePage;
