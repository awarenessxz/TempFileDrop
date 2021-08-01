import React, { MouseEvent, useState } from 'react';
import axios from "axios";
import { useDropzone } from 'react-dropzone';
import Button from '@material-ui/core/Button';
import Container from '@material-ui/core/Container';
import LinearProgress from '@material-ui/core/LinearProgress';
import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableContainer from '@material-ui/core/TableContainer';
import TableHead from '@material-ui/core/TableHead';
import TableRow from '@material-ui/core/TableRow';
import Paper from '@material-ui/core/Paper';
import { makeStyles, withStyles } from '@material-ui/core/styles';
import Spinner from "../Spinner/Spinner";
import { formatBytes } from "./FileDropzone.util";
import { FileDropzoneProps } from './FileDropzone.types';
import styles from './FileDropzone.module.scss';

const BorderLinearProgress = withStyles((theme) => ({
    root: {
        height: 10,
        borderRadius: 5,
    },
    colorPrimary: {
        backgroundColor: theme.palette.grey[theme.palette.type === 'light' ? 200 : 700],
    },
    bar: {
        borderRadius: 5,
        backgroundColor: '#1a90ff',
    },
}))(LinearProgress);

const useStyles = makeStyles({
    table: {
        minWidth: 650,
    },
});

/**
 * FileDropzone Component for File Upload
 */
const FileDropzone = ({
    uploadUrl,
    showDrops = false,
    maxSizeInBytes = 5368706371, // 5GB
    onSuccessfulUploadCallback = (res) => {},
    onErrorCallback = (err) => {}
}: FileDropzoneProps): JSX.Element => {
    const classes = useStyles();
    const { fileRejections, getRootProps, getInputProps } = useDropzone({
        onDrop: acceptedFiles => setFiles(acceptedFiles),
        maxSize: maxSizeInBytes
    });
    const [files, setFiles] = useState<File[]>([]);
    const uploadExceeded = (files.reduce((a, b) => a + b.size, 0) + fileRejections.reduce((a, b) => a + b.file.size, 0)) > maxSizeInBytes;
    const [errorMsg, setErrorMsg] = useState("");
    const [loading, setLoading] = useState(false);
    const [uploadPercentage, setUploadPercentage] = useState(0);
    const [uploadSuccess, setUploadSuccess] = useState(false);

    const handleUpload = (e: MouseEvent<HTMLButtonElement>) => {
        // reset states
        setUploadPercentage(0);
        setLoading(true);

        // upload files
        const formData = new FormData();
        files.forEach(file => formData.append("files", file));
        const options = {
            onUploadProgress: (progressEvent: any) => {
                const {loaded, total} = progressEvent;
                const percent = Math.floor((loaded * 100) / total);
                if (percent <= 100) {
                    setUploadPercentage(percent);
                }
            },
        };
        axios.post(uploadUrl, formData, options)
            .then(res => {
                setLoading(false);
                setUploadSuccess(true);
                onSuccessfulUploadCallback(res);
            })
            .catch(err => {
                setLoading(false);
                onErrorCallback(err);
                setErrorMsg("Upload Failed!");
            });
    };

    const handleReset = (e: MouseEvent<HTMLButtonElement>) => {
        setFiles([]);
        setUploadSuccess(false);
    };

    return (
        <Container className={styles.dropzoneContainer}>
            <div className={styles.dropzoneInputBox}>
                <div id="dropzone" {...getRootProps({ className: styles.dropzone })}>
                    <input {...getInputProps()} />
                    <svg className={styles.dropIcon} width="50" height="43" viewBox="0 0 50 43">
                        <path d="M48.4 26.5c-.9 0-1.7.7-1.7 1.7v11.6h-43.3v-11.6c0-.9-.7-1.7-1.7-1.7s-1.7.7-1.7 1.7v13.2c0 .9.7 1.7 1.7 1.7h46.7c.9 0 1.7-.7 1.7-1.7v-13.2c0-1-.7-1.7-1.7-1.7zm-24.5 6.1c.3.3.8.5 1.2.5.4 0 .9-.2 1.2-.5l10-11.6c.7-.7.7-1.7 0-2.4s-1.7-.7-2.4 0l-7.1 8.3v-25.3c0-.9-.7-1.7-1.7-1.7s-1.7.7-1.7 1.7v25.3l-7.1-8.3c-.7-.7-1.7-.7-2.4 0s-.7 1.7 0 2.4l10 11.6z"></path>
                    </svg>
                    {uploadExceeded ? (
                        <div className="text-danger mt-2">
                            Upload is too large. Max upload size is {formatBytes(maxSizeInBytes)}.
                        </div>
                    ) : (
                        <div>
                            {files.length === 0
                                ? <p>Drag and drop your files here, or click to select files.</p>
                                : <p>{files.length} files selected. {fileRejections.length > 0 && `${fileRejections.length} files rejected.`}</p>
                            }
                        </div>
                    )}
                </div>
                {files.length > 0 && !uploadExceeded && (
                    <div className={styles.actionBox}>
                        <Button variant="contained" color="primary" onClick={handleUpload}>Upload</Button>
                    </div>
                )}
                {errorMsg && (
                    <div className={`${styles.dropzoneBox} ${styles.messageError}`}>
                        {errorMsg}
                    </div>
                )}
                {loading && uploadPercentage < 100 && (
                    <div className={`${styles.dropzoneBox} ${styles.loader}`}>
                        <BorderLinearProgress variant="determinate" value={uploadPercentage} />
                    </div>
                )}
                {uploadSuccess && (
                    <div className={`${styles.dropzoneBox} ${styles.messageSuccess}`}>
                        <p>Files have been uploaded!</p>
                        <div className={styles.actionBox}>
                            <Button variant="contained" color="primary" onClick={handleReset}>Upload Again</Button>
                        </div>
                    </div>
                )}
                {loading && uploadPercentage >= 100 && <Spinner spinnerType="ThreeDots" backgroundColor="#92b0b3" spinnerColor="#fff" />}
            </div>
            {showDrops && files.length > 0 && (
                <div className={styles.dropzoneResultBox}>
                    <TableContainer component={Paper}>
                        <Table className={classes.table} aria-label="simple table">
                            <TableHead>
                                <TableRow>
                                    <TableCell>#</TableCell>
                                    <TableCell>Filename</TableCell>
                                    <TableCell>File Size</TableCell>
                                </TableRow>
                            </TableHead>
                            <TableBody>
                                {files.map((file, idx) => (
                                    <TableRow key={idx} data-testid={`${file.name}-${idx}`}>
                                        <TableCell component="th" scope="row">{idx+1}</TableCell>
                                        <TableCell component="th" scope="row">{file.name}</TableCell>
                                        <TableCell component="th" scope="row">{file.size}</TableCell>
                                    </TableRow>
                                ))}
                            </TableBody>
                        </Table>
                    </TableContainer>
                </div>
            )}
        </Container>
    );
};

export default FileDropzone;
