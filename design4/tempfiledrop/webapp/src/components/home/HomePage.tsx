import React, { useState } from "react";
import Button from "react-bootstrap/cjs/Button";
import Container from "react-bootstrap/cjs/Container";
import VerticallyCenteredModal from "../common/modal/VerticallyCenteredModal";
import FileDropzone from "../common/dropzone/FileDropzone";
import { useAuthState } from "../../utils/auth-context";
import "./HomePage.css";

const HomePage = () => {
    const [modalShow, setModalShow] = useState(false);
    const { isAuthenticated } = useAuthState();

    return (
        <React.Fragment>
            <header className="masthead">
                <Container>
                    <div className="mx-auto text-center">
                        <h1>TempFileDrop.io</h1>
                        <h3>Convenient way to share your files</h3>
                        <Button size="lg" onClick={() => setModalShow(true)}>
                            Upload
                        </Button>
                    </div>
                </Container>
            </header>
            <VerticallyCenteredModal title="Upload Files" content={<FileDropzone showConfigs={isAuthenticated} />} show={modalShow} onHide={() => setModalShow(false)} />
        </React.Fragment>
    );
};

export default HomePage;
