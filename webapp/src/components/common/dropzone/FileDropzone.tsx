import React, { MouseEvent, useState, useRef } from "react";
import axios from "axios";
import { FaCopy } from "react-icons/fa";
import { useDropzone } from 'react-dropzone'
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
import { FileUploadRequest, FileUploadResponse } from "../../../types/api-types"
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
    const { userInfo } = useAuthState();
    const { acceptedFiles, fileRejections, getRootProps, getInputProps } = useDropzone({
        maxSize: Data.dropzone.maxSizeInBytes
    });
    const isFileTooLarge = fileRejections.length > 0 && fileRejections[0].file.size > Data.dropzone.maxSizeInBytes;
    const [errorMsg, setErrorMsg] = useState("");
    const [uploadRes, setUploadRes] = useState<FileUploadResponse|null>(null);
    const [loading, setLoading] = useState(false);
    const [uploadPercentage, setUploadPercentage] = useState(0);
    const [maxDownloads, setMaxDownloads] = useState<number|"">("");
    const [copiedText, setCopiedText] = useState("Copy to Clipboard");
    const [downloadLink, setDownloadLink] = useState("");
    const downloadLinkRef = useRef(null);
    const selectRef = useRef(null);

    const handleUpload = (e: MouseEvent<HTMLButtonElement>) => {
        // reset states
        setUploadPercentage(0);
        setLoading(true);

        // craft payload
        const formData = new FormData();
        acceptedFiles.forEach(file => {
            formData.append("files", file);
        });
        // @ts-ignore
        const expiryPeriod = selectRef.current === null ? 1 : selectRef.current.options.selectedIndex;
        const username = userInfo === null ? "" : userInfo.username;
        const metadata: FileUploadRequest = {
            bucket: Data.bucket,
            storagePath: username === "" ? "anonymous" : username,
            maxDownloads: maxDownloads === "" ? 1 : maxDownloads,
            expiryPeriod,
            eventRoutingKey: "tempfiledrop_upload",
            eventData: JSON.stringify({ username: username })
        };
        formData.append("metadata", new Blob([JSON.stringify(metadata)], {
            type: "application/json"
        }));

        // set upload percentage
        const options = {
            onUploadProgress: (progressEvent: any) => {
                const {loaded, total} = progressEvent;
                const percent = Math.floor((loaded * 100) / total);
                if (percent <= 100) {
                    setUploadPercentage(percent);
                }
            }
        };

        // send request
        axios.post(Data.api_endpoints.storagesvc_upload, formData, options)
            .then(res => {
                setLoading(false);
                if (res.status === 200) {
                    const fileUploadResponse: FileUploadResponse = res.data;
                    setDownloadLink(joinURLs(window.location.origin, "download", fileUploadResponse.storageId));
                    setUploadRes(fileUploadResponse);
                    onSuccessfulUploadCallback();
                } else {
                    setErrorMsg("Upload Failed!");
                }
            })
            .catch(err => {
                console.log(err.message);
                setLoading(false);
                setErrorMsg("Server Error! Please try again later...");
            });
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
                    {acceptedFiles.length === 0
                        ? <p>Drag and drop your files here, or click to select files.</p>
                        : <p>{acceptedFiles.length} files selected</p>
                    }
                    {isFileTooLarge && (
                        <div className="text-danger mt-2">
                            File is too large. Max file size is 5GB.
                        </div>
                    )}
                </div>
                {acceptedFiles.length > 0 && (
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
