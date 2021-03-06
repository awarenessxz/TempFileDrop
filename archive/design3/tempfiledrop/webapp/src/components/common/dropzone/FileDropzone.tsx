import React, { MouseEvent, useState, useRef } from "react";
import { FaCopy } from "react-icons/fa";
import { useDropzone } from 'react-dropzone'
import StorageClient, { FileUploadMetadata, FileUploadResponse } from "storage-js-client";
import Button from "react-bootstrap/cjs/Button";
import Col from "react-bootstrap/cjs/Col";
import Container from "react-bootstrap/cjs/Container";
import Form from "react-bootstrap/cjs/Form";
import ProgressBar from "react-bootstrap/cjs/ProgressBar";
import Row from "react-bootstrap/cjs/Row";
import Table from "react-bootstrap/cjs/Table";
import Spinner from "../loading/Spinner";
import { useAuthState } from "../../../utils/auth-context";
import { joinURLs } from "../../../utils/toolkit";
import Data from "../../../config/app.json";
import "./FileDropzone.css";

interface FileDropzoneProps {
    showUploads?: boolean;
    showConfigs?: boolean;
    onSuccessfulUploadCallback?: () => void;
}

const ExpiryPeriod = ["1 Hour", "1 Day", "1 Week"];

const FileDropzone = ({
    showUploads = false,
    showConfigs = false,
    onSuccessfulUploadCallback = () => {}
}: FileDropzoneProps) => {
    const { userToken, isAuthenticated } = useAuthState();
    const maxUploadSize = isAuthenticated ? Data.dropzone.maxSizeInBytes : Data.dropzone.maxSizeInBytesForAnonymous;
    const { acceptedFiles, fileRejections, getRootProps, getInputProps } = useDropzone({
        maxSize: maxUploadSize
    });
    const uploadExceeded = (acceptedFiles.reduce((a, b) => a + b.size, 0) + fileRejections.reduce((a, b) => a + b.file.size, 0)) > maxUploadSize;
    const [errorMsg, setErrorMsg] = useState("");
    const [uploadRes, setUploadRes] = useState<FileUploadResponse|null>(null);
    const [loading, setLoading] = useState(false);
    const [uploadPercentage, setUploadPercentage] = useState(0);
    const [maxDownloads, setMaxDownloads] = useState<number|"">("");
    const [copiedText, setCopiedText] = useState("Copy to Clipboard");
    const [downloadLink, setDownloadLink] = useState("");
    const downloadLinkRef = useRef(null);
    const anonDownloadRef = useRef(null);
    const selectRef = useRef(null);

    const handleUpload = (e: MouseEvent<HTMLButtonElement>) => {
        // reset states
        setUploadPercentage(0);
        setLoading(true);

        const handleUploadSuccess = (uploadRes: FileUploadResponse) => {
            setLoading(false);
            setDownloadLink(joinURLs(window.location.origin, "download", uploadRes.storageId));
            setUploadRes(uploadRes);
        };

        const handleUploadFailure = (err: any) => {
            setLoading(false);
            setErrorMsg("Upload Failed!");
        };

        if (isAuthenticated) {
            // @ts-ignore
            const expiryPeriod = selectRef.current === null ? 1 : selectRef.current.options.selectedIndex;
            // @ts-ignore
            const anonDownload = anonDownloadRef.current === null ? false : anonDownloadRef.current.checked;
            const metadata: FileUploadMetadata = {
                bucket: Data.bucket,
                storagePath: userToken ? userToken.username : "",
                maxDownloads: maxDownloads === "" ? 1 : maxDownloads,
                expiryPeriod,
                allowAnonymousDownload: anonDownload,
                eventRoutingKey: Data.rabbitmq.routingkey,
                eventData: JSON.stringify({ username: userToken?.username })
            };
            const token = window.accessToken ? window.accessToken : "dummy_token";
            StorageClient.upload({
                url: Data.api_endpoints.storagesvc_upload,
                files: acceptedFiles,
                metadata: metadata,
                headers: {
                    'Authorization': 'Bearer ' + token
                },
                onUploadPercentage: (percentage) => setUploadPercentage(percentage),
                onError: handleUploadFailure,
                onSuccess: handleUploadSuccess,
            });
        } else {
            StorageClient.uploadAnonymously({
                url: Data.api_endpoints.storagesvc_upload_anonymous,
                files: acceptedFiles,
                onUploadPercentage: (percentage) => setUploadPercentage(percentage),
                onError: handleUploadFailure,
                onSuccess: handleUploadSuccess,
            });
        }
    };

    const copyToClipboard = (e: MouseEvent<HTMLButtonElement>) => {
        if (downloadLinkRef.current !== null) {
            // @ts-ignore
            downloadLinkRef.current.select();
            document.execCommand('copy');
            setCopiedText("Copied!");
        }
    };

    return (
        <Container className="dropzone-container">
            <div className="dropzone-input-box">
                <div {...getRootProps({ className: "dropzone" })}>
                    <input {...getInputProps()} />
                    <svg className="drop-icon" width="50" height="43" viewBox="0 0 50 43">
                        <path d="M48.4 26.5c-.9 0-1.7.7-1.7 1.7v11.6h-43.3v-11.6c0-.9-.7-1.7-1.7-1.7s-1.7.7-1.7 1.7v13.2c0 .9.7 1.7 1.7 1.7h46.7c.9 0 1.7-.7 1.7-1.7v-13.2c0-1-.7-1.7-1.7-1.7zm-24.5 6.1c.3.3.8.5 1.2.5.4 0 .9-.2 1.2-.5l10-11.6c.7-.7.7-1.7 0-2.4s-1.7-.7-2.4 0l-7.1 8.3v-25.3c0-.9-.7-1.7-1.7-1.7s-1.7.7-1.7 1.7v25.3l-7.1-8.3c-.7-.7-1.7-.7-2.4 0s-.7 1.7 0 2.4l10 11.6z"></path>
                    </svg>
                    {uploadExceeded ? (
                        <div className="text-danger mt-2">
                            Upload is too large. Max upload size is {isAuthenticated ? Data.dropzone.maxSizeInText : Data.dropzone.maxSizeInTextForAnonymous}.
                        </div>
                    ) : (
                        <div>
                            {acceptedFiles.length === 0
                                ? <p>Drag and drop your files here, or click to select files.</p>
                                : <p>{acceptedFiles.length} files selected. {fileRejections.length > 0 && `${fileRejections.length} files rejected.`}</p>
                            }
                        </div>
                    )}
                </div>
                {acceptedFiles.length > 0 && !uploadExceeded && (
                    <div className="action-box">
                        <Button size="lg" onClick={handleUpload}>Upload</Button>
                    </div>
                )}
                {errorMsg && (
                    <div className="dropzone-box message-error">
                        {errorMsg}
                    </div>
                )}
                {uploadRes && (
                    <div className="dropzone-box message-success">
                        {uploadRes.message}
                        <div className="dropzone-share-link">
                            <input ref={downloadLinkRef} value={downloadLink} onChange={() => {}}/>
                            <div className="copy-tooltip">
                                <Button onClick={copyToClipboard} onMouseOut={() => setCopiedText("Copy to Clipboard")}>
                                    <span className="copy-tooltiptext">{copiedText}</span>
                                    <FaCopy />
                                </Button>
                            </div>
                        </div>
                    </div>
                )}
                {loading && uploadPercentage < 100 && (
                    <div className="dropzone-box loader">
                        <ProgressBar striped now={uploadPercentage} animated />
                    </div>
                )}
                {loading && uploadPercentage >= 100 && <Spinner spinnerType="ThreeDots" backgroundColor="#92b0b3" spinnerColor="#fff" />}
            </div>
            {showConfigs && !loading && !uploadRes && (
                <div className="dropzone-config-box">
                    <h5>Upload Settings</h5>
                    <Form>
                        <Form.Group as={Row} controlId="formHorizontalMaxDownloads">
                            <Form.Label column sm={4}>
                                Max Downloads
                            </Form.Label>
                            <Col sm={8}>
                                <Form.Control
                                    type="text"
                                    pattern="[0-9]*"
                                    placeholder="Maximum number of downloads (Default = 1)"
                                    value={maxDownloads}
                                    onChange={e => {
                                        const re = /^[0-9\b]+$/;
                                        if (e.target.value === '' || re.test(e.target.value)) {
                                            if (e.target.value === '') {
                                                setMaxDownloads("");
                                            } else {
                                                setMaxDownloads(parseInt(e.target.value));
                                            }
                                        }
                                    }}
                                />
                            </Col>
                        </Form.Group>
                        <Form.Group as={Row} controlId="formHorizontalExpiry">
                            <Form.Label column sm={4}>
                                Expiry Period
                            </Form.Label>
                            <Col sm={8}>
                                <Form.Control as="select" custom ref={selectRef}>
                                    {ExpiryPeriod.map((option, idx) => <option key={idx}>{option}</option>)}
                                </Form.Control>
                            </Col>
                        </Form.Group>
                        <Form.Group as={Row} controlId="formHorizontalAnonymousDownload" className="align-center-row">
                            <Form.Label column sm={4}>
                                Allow Anonymous Download?
                            </Form.Label>
                            <Col sm={8}>
                                <Form.Check type="switch" label="" id="anonDownloadSwitch" ref={anonDownloadRef} />
                            </Col>
                        </Form.Group>
                    </Form>
                </div>
            )}
            {showUploads && acceptedFiles.length > 0 && (
                <div className="dropzone-result-box">
                    <Table responsive size="sm">
                        <thead>
                            <tr>
                                <th>#</th>
                                <th>Filename</th>
                                <th>File Size</th>
                            </tr>
                        </thead>
                        <tbody>
                            {acceptedFiles.map((file, idx) => (
                                <tr key={idx}>
                                    <td>{idx+1}</td>
                                    <td>{file.name}</td>
                                    <td>{file.size}</td>
                                </tr>
                            ))}
                        </tbody>
                    </Table>
                </div>
            )}
        </Container>
    );
};

export default FileDropzone;
