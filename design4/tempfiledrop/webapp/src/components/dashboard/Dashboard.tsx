import React, { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import axios from "axios";
import moment from "moment";
import StorageClient, { StorageMetadata } from "storage-js-client";
import { FaTrash, FaDownload } from "react-icons/fa";
import Alert from "react-bootstrap/cjs/Alert";
import Button from "react-bootstrap/cjs/Button";
import Col from "react-bootstrap/cjs/Col";
import Container from "react-bootstrap/cjs/Container";
import Row from "react-bootstrap/cjs/Row";
import Table from "react-bootstrap/cjs/Table";
import VerticallyCenteredModal from "../common/modal/VerticallyCenteredModal";
import FileDropzone from "../common/dropzone/FileDropzone";
import { useAuthState } from "../../utils/auth-context";
import { useSocketState } from "../../utils/socket-context";
import { joinURLs } from "../../utils/toolkit";
import Data from "../../config/app.json";
import "./Dashboard.css";

const Dashboard = () => {
    const [modalShow, setModalShow] = useState(false);
    const [records, setRecords] = useState<StorageMetadata[]>([]);
    const [displayMsg, setDisplayMsg] = useState("");
    const [isError, setIsError] = useState(false);
    const [rerender, setRerender] = useState(false);
    const authState = useAuthState();
    const socketState = useSocketState();

    useEffect(() => {
        if (authState || socketState) {
            const { userToken } = authState;
            if (userToken) {
                axios.get(`${Data.api_endpoints.uploaded_files}/${userToken.username}`)
                    .then(res => {
                        if (res.status === 200) {
                            const records: StorageMetadata[] = res.data;
                            setRecords(records);
                        }
                    })
                    .catch(err => {
                        setIsError(true);
                        setDisplayMsg("Fail to fetch records...");
                    });
            }
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [authState, socketState]);

    const handleDeleteRecord = (idx: number, objectName: string) => {
        // remove from view
        records.splice(idx, 1);
        setRecords([...records]);

        // call backend
        const token = window.accessToken ? window.accessToken : "dummy_token";
        StorageClient.deleteFromStorageService({
            storageObjects: [objectName],
            headers: { 'Authorization': 'Bearer ' + token },
            onSuccess: () => {
                setDisplayMsg("Delete Success!");
                setIsError(false);
                setRerender(!rerender);
            },
            onError: (err) => {
                console.error(err)
            }
        });
    };

    const handleDownloadRecord = (storageMetadata: StorageMetadata) => {
        const token = window.accessToken ? window.accessToken : "dummy_token";
        StorageClient.downloadViaPresignedUrl({
            metadata: {
                bucket: storageMetadata.bucket,
                storageObjects: [storageMetadata.objectName]
            },
            headers: { 'Authorization': 'Bearer ' + token },
            onError(err: any): void {
                console.log(err);
            },
            onSuccess(): void {
                setDisplayMsg("You have downloaded the files!");
                setRerender(!rerender);
            }
        });
    };

    const onSuccessfulUpload = () => {
        setDisplayMsg(""); // reset
        setRerender(!rerender);
    };

    return (
        <div className="dashboard-container">
            <Container>
                {displayMsg && (
                    <Row>
                        <Col>
                            <Alert variant={`${isError ? "danger" : "success" }`}>
                                {displayMsg}
                            </Alert>
                        </Col>
                    </Row>
                )}
                <Row>
                    <Col >
                        <h1>Dashboard</h1>
                    </Col>
                    <Col xs={4} className="align-right">
                        <Button size="lg" onClick={() => setModalShow(true)}>
                            Upload
                        </Button>
                    </Col>
                </Row>
                <Row>
                    <Table responsive size="sm">
                        <thead>
                        <tr>
                            <th>#</th>
                            <th>Files</th>
                            <th>Downloads Left</th>
                            <th>Expiry Datetime</th>
                            <th>Anon Download?</th>
                            <th>Download Link</th>
                            <th>Actions</th>
                        </tr>
                        </thead>
                        <tbody>
                        {records.map((record, idx) => (
                            <tr key={idx}>
                                <td>{idx+1}</td>
                                <td>{record.objectName}</td>
                                <td>{record.numOfDownloadsLeft}</td>
                                <td>{moment(record.expiryDatetime).format("DD MMM YYYY h:mma")}</td>
                                <td>{record.allowAnonymousDownload ? "Yes" : "No"}</td>
                                <td><Link to={`/download/${record.objectName}`}>{joinURLs(window.location.origin, "tempfiledrop", "download", record.objectName)}</Link></td>
                                <td className="action-btn-group">
                                    <Button className="action-btn" size="sm" variant="info" onClick={() => handleDownloadRecord(record)}>
                                        <FaDownload />
                                    </Button>
                                    <Button className="action-btn" size="sm" variant="danger" onClick={() => handleDeleteRecord(idx, record.objectName)}>
                                        <FaTrash />
                                    </Button>
                                </td>
                            </tr>
                        ))}
                        </tbody>
                    </Table>
                </Row>
                {records.length === 0 && (
                    <Row>
                        <Col>
                            <Alert variant="secondary" className="text-center">
                                No records available
                            </Alert>
                        </Col>
                    </Row>
                )}
            </Container>
            <VerticallyCenteredModal
                title="Upload Files"
                content={<FileDropzone showConfigs={true} onSuccessfulUploadCallback={onSuccessfulUpload}/>}
                show={modalShow}
                onHide={() => setModalShow(false)}
            />
        </div>
    );
};

export default Dashboard;
