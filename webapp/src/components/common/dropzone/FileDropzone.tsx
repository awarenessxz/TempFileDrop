import React, { MouseEvent, useState } from "react";
import axios from "axios";
import { useDropzone } from 'react-dropzone'
import Button from "react-bootstrap/cjs/Button";
import Container from "react-bootstrap/cjs/Container";
import Table from "react-bootstrap/cjs/Table";
import Spinner from "../loading/Spinner";
import { FileStorageResponse } from "../../../types/upload-type";
import "./FileDropzone.css";

interface FileDropzoneProps {
    showUploads?: boolean;
}

const FileDropzone = ({
    showUploads = false
}: FileDropzoneProps) => {
    const { acceptedFiles, fileRejections, getRootProps, getInputProps } = useDropzone();
    const [errorMsg, setErrorMsg] = useState("");
    const [uploadRes, setUploadRes] = useState<FileStorageResponse|null>(null);
    const [loading, setLoading] = useState(false);

    const handleUpload = (e: MouseEvent<HTMLButtonElement>) => {
        // reset states
        setLoading(true);

        // craft payload & fetch
        const formData = new FormData();
        acceptedFiles.forEach(file => {
            formData.append("file", file);
        });
        axios.post("/files/upload", formData, {})
            .then(res => {
                setLoading(false);
                if (res.status === 200) {
                    setUploadRes(res.data);
                } else {
                    setErrorMsg("Upload Failed!");
                }
            })
            .catch(err => {
                console.log(err);
                setLoading(false);
                setErrorMsg("Server Error! Please try again later...");
            });
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
                </div>
                {acceptedFiles.length > 0 && (
                    <div className="action-box">
                        <Button size="lg" onClick={handleUpload}>Upload</Button>
                    </div>
                )}
                {errorMsg && (
                    <div className="dropzone-message-box message-error">
                        {errorMsg}
                    </div>
                )}
                {uploadRes && (
                    <div className="dropzone-message-box message-success">
                        {uploadRes.message}
                    </div>
                )}
                {loading && <Spinner spinnerType="ThreeDots" backgroundColor="#92b0b3" spinnerColor="#fff" />}
            </div>
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
                                <tr>
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
