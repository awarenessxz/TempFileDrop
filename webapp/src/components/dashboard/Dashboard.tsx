import React, { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import axios from "axios";
import { FaTrash } from "react-icons/fa";
import Alert from "react-bootstrap/cjs/Alert";
import Button from "react-bootstrap/cjs/Button";
import Col from "react-bootstrap/cjs/Col";
import Container from "react-bootstrap/cjs/Container";
import Row from "react-bootstrap/cjs/Row";
import Table from "react-bootstrap/cjs/Table";
import VerticallyCenteredModal from "../common/modal/VerticallyCenteredModal";
import FileDropzone from "../common/dropzone/FileDropzone";
import { useAuthState } from "../../utils/auth-context";
import { joinURLs } from "../../utils/toolkit";
import Data from "../../config/app.json";
import { UploadedFiles } from "../../types/api-types";
import "./Dashboard.css";
import moment from "moment";

const Dashboard = () => {
    const [modalShow, setModalShow] = useState(false);
    const [records, setRecords] = useState<UploadedFiles[]>([]);
    const [reloadRecord, setReloadRecord] = useState(false);    // trigger to reload
    const [message, setMessage] = useState("");
    const [isError, setIsError] = useState(false);
    const { userInfo } = useAuthState();

    useEffect(() => {
        axios.get(`${Data.api_endpoints.uploaded_files}/${userInfo?.username}`)
            .then(res => {
                if (res.status === 200) {
                    const records: UploadedFiles[] = res.data;
                    setRecords(records);
                }
            })
            .catch(err => {
                setIsError(true)
                setMessage("Fail to fetch records...");
            });
        // eslint-disable-next-line
    }, [reloadRecord]);

    const handleDeleteRecord = (idx: number, recordId: string) => {
        // remove from view
        records.splice(idx, 1);
        setRecords([...records]);

        // call backend
        axios.delete(`${Data.api_endpoints.uploaded_files}/user1/${recordId}`)
            .then(res => {
                if (res.status === 200) {
                    setMessage("Delete Success!");
                    setIsError(false);
                }
            })
            .catch(err => console.log(err));
    };

    const onSuccessfulUpload = () => {
        setReloadRecord(!reloadRecord);
    };

    return (
        <div className="dashboard-container">
            <Container>
                {message && (
                    <Row>
                        <Col>
                            <Alert variant={`${isError ? "danger" : "success" }`}>
                                {message}
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
                            <th>Download Link</th>
                            <th>Actions</th>
                        </tr>
                        </thead>
                        <tbody>
                        {records.map((record, idx) => (
                            <tr key={idx}>
                                <td>{idx+1}</td>
                                <td>{record.filenames}</td>
                                <td>{record.numOfDownloadsLeft}</td>
                                <td>{moment(record.expiryDatetime).format("DD MMM YYYY h:mma")}</td>
                                <td><Link to={`/download/${record.storageId}`}>{joinURLs(window.location.origin, "download", record.storageId)}</Link></td>
                                <td>
                                    <Button size="sm" variant="danger" onClick={() => handleDeleteRecord(idx, record.id)}>
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
