import React, { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import axios from "axios";
import moment from "moment";
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
import { extractFilenameFromContentDisposition, joinURLs } from "../../utils/toolkit";
import Data from "../../config/app.json";
import { UserUploadInfo } from "../../types/api-types";
import "./Dashboard.css";

const Dashboard = () => {
    const [modalShow, setModalShow] = useState(false);
    const [records, setRecords] = useState<UserUploadInfo[]>([]);
    const [reloadRecord, setReloadRecord] = useState(false);    // trigger to reload
    const [message, setMessage] = useState("");
    const [isError, setIsError] = useState(false);
    const { userToken } = useAuthState();

    useEffect(() => {
        if (userToken) {
            axios.get(`${Data.api_endpoints.uploaded_files}/${userToken.username}`)
                .then(res => {
                    if (res.status === 200) {
                        const records: UserUploadInfo[] = res.data;
                        setRecords(records);
                    }
                })
                .catch(err => {
                    setIsError(true)
                    setMessage("Fail to fetch records...");
                });
        }
    }, [reloadRecord, userToken]);

    const handleDeleteRecord = (idx: number, recordId: string, storageId: string) => {
        // remove from view
        records.splice(idx, 1);
        setRecords([...records]);

        // call backend
        axios.delete(`${Data.api_endpoints.storagesvc}/${Data.bucket}/${storageId}`, {
            params: {
                eventData: JSON.stringify({ recordId: recordId }),
                eventRoutingKey: Data.rabbitmq.routingkey
            }
        })
            .then(res => {
                if (res.status === 200) {
                    setMessage("Delete Success!");
                    setIsError(false);
                }
            })
            .catch(err => console.log(err));
    };

    const handleDownloadRecord = (storageId: string) => {
        axios.get(`${Data.api_endpoints.storagesvc_download}/${Data.bucket}/${storageId}`, {
            responseType: "blob",
            params: {
                eventRoutingKey: Data.rabbitmq.routingkey
            }
        })
            .then(res => {
                const filename = extractFilenameFromContentDisposition(res.headers);
                const url = window.URL.createObjectURL(new Blob([res.data]));
                const link = document.createElement('a');
                link.href = url;
                link.setAttribute('download', filename);
                document.body.appendChild(link);
                link.click();
                setMessage("You have downloaded the files!");
                setReloadRecord(!reloadRecord);
            })
            .catch(err => {
                console.log(err);
            });
    };

    const onSuccessfulUpload = () => {
        setMessage(""); // reset
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
                                <td>{record.storageInfo.filenames}</td>
                                <td>{record.storageInfo.numOfDownloadsLeft}</td>
                                <td>{moment(record.storageInfo.expiryDatetime).format("DD MMM YYYY h:mma")}</td>
                                <td><Link to={`/download/${record.storageInfo.storageId}`}>{joinURLs(window.location.origin, "download", record.storageInfo.storageId)}</Link></td>
                                <td className="action-btn-group">
                                    <Button className="action-btn" size="sm" variant="info" onClick={() => handleDownloadRecord(record.storageInfo.storageId)}>
                                        <FaDownload />
                                    </Button>
                                    <Button className="action-btn" size="sm" variant="danger" onClick={() => handleDeleteRecord(idx, record.id, record.storageInfo.storageId)}>
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
