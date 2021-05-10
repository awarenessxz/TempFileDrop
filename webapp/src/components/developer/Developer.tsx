import React from "react";
import SwaggerUI from "swagger-ui-react";
import "swagger-ui-react/swagger-ui.css";
import Container from "react-bootstrap/cjs/Container";
import Data from "../../config/app.json";

const Developer = () => {
    return (
        <Container>
            <SwaggerUI url={Data.api_endpoints.storagesvc_swagger} />
        </Container>
    );
};

export default Developer;
