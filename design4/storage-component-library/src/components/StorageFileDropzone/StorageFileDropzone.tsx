import React, { MouseEvent, useRef, useState } from 'react';
import { useDropzone } from 'react-dropzone';
import Button from '@material-ui/core/Button';
import Container from '@material-ui/core/Container';
import FileCopyIcon from '@material-ui/icons/FileCopy';
import FormGroup from '@material-ui/core/FormGroup';
import FormControlLabel from '@material-ui/core/FormControlLabel';
import Checkbox from '@material-ui/core/Checkbox';
import FormControl from '@material-ui/core/FormControl';
import FormHelperText from '@material-ui/core/FormHelperText';
import Select from '@material-ui/core/Select';
import LinearProgress from '@material-ui/core/LinearProgress';
import MenuItem from '@material-ui/core/MenuItem';
import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableContainer from '@material-ui/core/TableContainer';
import TableHead from '@material-ui/core/TableHead';
import TableRow from '@material-ui/core/TableRow';
import TextField from '@material-ui/core/TextField';
import Paper from '@material-ui/core/Paper';
import { makeStyles, withStyles } from '@material-ui/core/styles';
import Spinner from "../Spinner/Spinner";
import StorageClient, { FileUploadMetadata, FileUploadResponse } from "storage-js-client";
import { ExpiryPeriod, joinURLs, formatBytes } from "./StorageFileDropzone.util";
import { StorageFileDropzoneProps } from './StorageFileDropzone.types';
import styles from "./StorageFileDropzone.module.scss";

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
 * StorageFileDropzone Component for File Upload
 */
const StorageFileDropzone = ({
      showDrops = false,
      showConfigs = false,
      isAnonymousUpload = false,
      maxSizeInBytes = 5368706371, // 5GB
      uploadMetadata = undefined,
      onSuccessfulUploadCallback = () => {},
      onErrorCallback = (err) => {}
}: StorageFileDropzoneProps): JSX.Element => {
    const classes = useStyles();
    const { acceptedFiles, fileRejections, getRootProps, getInputProps } = useDropzone({
        maxSize: maxSizeInBytes
    });
    const uploadExceeded = (acceptedFiles.reduce((a, b) => a + b.size, 0) + fileRejections.reduce((a, b) => a + b.file.size, 0)) > maxSizeInBytes;
    const [errorMsg, setErrorMsg] = useState("");
    const [uploadRes, setUploadRes] = useState<FileUploadResponse|null>(null);
    const [loading, setLoading] = useState(false);
    const [uploadPercentage, setUploadPercentage] = useState(0);
    const [copiedText, setCopiedText] = useState("Copy to Clipboard");
    const [downloadLink, setDownloadLink] = useState("");
    const [maxDownloads, setMaxDownloads] = useState<number|"">("");
    const [isAnonDownloadChecked, setIsAnonDownloadChecked] = useState(false);
    const [expiryPeriodIdx, setExpiryPeriodIdx] = useState(0);
    const downloadLinkRef = useRef(null);

    const handleUpload = (e: MouseEvent<HTMLButtonElement>) => {
        // reset states
        setUploadPercentage(0);
        setLoading(true);

        const handleUploadSuccess = (uploadRes: FileUploadResponse) => {
            setLoading(false);
            setDownloadLink(joinURLs(window.location.origin, "download", uploadRes.storageId));
            setUploadRes(uploadRes);
            onSuccessfulUploadCallback();
        };

        const handleUploadFailure = (err: any) => {
            setLoading(false);
            setErrorMsg("Upload Failed!");
            onErrorCallback(err);
        };

        if (isAnonymousUpload) {
            StorageClient.uploadAnonymously({
                url: "/api/storagesvc/anonymous/upload",
                files: acceptedFiles,
                onUploadPercentage: (percentage) => setUploadPercentage(percentage),
                onError: handleUploadFailure,
                onSuccess: handleUploadSuccess,
            });
        } else {
            if (uploadMetadata === undefined) {
                setErrorMsg("FileDropzone is missing metadata! Please provide metadata parameters...");
            } else {
                const metadata: FileUploadMetadata = {
                    bucket: uploadMetadata.bucket,
                    storagePath: uploadMetadata.storagePath,
                    maxDownloads: maxDownloads === "" ? 1 : maxDownloads,
                    expiryPeriod: expiryPeriodIdx,
                    allowAnonymousDownload: isAnonDownloadChecked,
                    eventRoutingKey: uploadMetadata.eventRoutingKey,
                    eventData: uploadMetadata?.eventData
                };
                StorageClient.upload({
                    url: "/api/storagesvc/upload",
                    files: acceptedFiles,
                    metadata: metadata,
                    onUploadPercentage: (percentage) => setUploadPercentage(percentage),
                    onError: handleUploadFailure,
                    onSuccess: handleUploadSuccess,
                });
            }
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
                            {acceptedFiles.length === 0
                                ? <p>Drag and drop your files here, or click to select files.</p>
                                : <p>{acceptedFiles.length} files selected. {fileRejections.length > 0 && `${fileRejections.length} files rejected.`}</p>
                            }
                        </div>
                    )}
                </div>
                {acceptedFiles.length > 0 && !uploadExceeded && (
                    <div className={styles.actionBox}>
                        <Button variant="contained" color="primary" onClick={handleUpload}>Upload</Button>
                    </div>
                )}
                {errorMsg && (
                    <div className={`${styles.dropzoneBox} ${styles.messageError}`}>
                        {errorMsg}
                    </div>
                )}
                {uploadRes && (
                    <div className={`${styles.dropzoneBox} ${styles.messageSuccess}`}>
                        <p>{uploadRes.message}</p>
                        <div className={styles.dropzoneShareLink}>
                            <input ref={downloadLinkRef} value={downloadLink} onChange={() => {}}/>
                            <div className={styles.copyTooltip}>
                                <Button variant="contained" color="primary" onClick={copyToClipboard} onMouseOut={() => setCopiedText("Copy to Clipboard")}>
                                    <span className={styles.copyTooltiptext}>{copiedText}</span>
                                    <FileCopyIcon />
                                </Button>
                            </div>
                        </div>
                    </div>
                )}
                {loading && uploadPercentage < 100 && (
                    <div className={`${styles.dropzoneBox} ${styles.loader}`}>
                        <BorderLinearProgress variant="determinate" value={uploadPercentage} />
                    </div>
                )}
                {loading && uploadPercentage >= 100 && <Spinner spinnerType="ThreeDots" backgroundColor="#92b0b3" spinnerColor="#fff" />}
            </div>
            {showConfigs && !isAnonymousUpload && !loading && !uploadRes && (
                <div className={styles.dropzoneConfigBox}>
                    <h5>Upload Settings</h5>
                    <div className={styles.dropzoneConfigRow}>
                        <TextField
                            fullWidth
                            label="Max Downloads"
                            placeholder="Maximum number of downloads (Default = 1)"
                            type="text"
                            variant="outlined"
                            value={maxDownloads}
                            onChange={(event) => {
                                const re = /^[0-9\b]+$/;
                                if (event.target.value === '' || re.test(event.target.value)) {
                                    if (event.target.value === '') {
                                        setMaxDownloads("");
                                    } else {
                                        setMaxDownloads(parseInt(event.target.value));
                                    }
                                }
                            }}
                        />
                    </div>
                    <FormControl>
                        <div className={styles.dropzoneConfigRow}>
                            <Select
                                labelId="expiry-label"
                                value={expiryPeriodIdx}
                                onChange={(event) => setExpiryPeriodIdx(event.target.value as number)}
                                fullWidth
                            >
                                {ExpiryPeriod.map((option, idx) => <MenuItem key={idx} value={idx}>{option}</MenuItem>)}
                            </Select>
                            <FormHelperText>Expiry Period</FormHelperText>
                        </div>
                    </FormControl>
                    <FormGroup row className={styles.dropzoneConfigRow}>
                        <FormControlLabel
                            control={
                                <Checkbox
                                    checked={isAnonDownloadChecked}
                                    onChange={() => setIsAnonDownloadChecked(!isAnonDownloadChecked)}
                                    name="IsAnonDownload"
                                    color="primary"/>
                            }
                            label="Allow Anonymous Download?"
                        />
                    </FormGroup>
                </div>
            )}
            {showDrops && acceptedFiles.length > 0 && (
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
                                {acceptedFiles.map((file, idx) => (
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

export default StorageFileDropzone;
