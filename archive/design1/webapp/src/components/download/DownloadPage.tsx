import axios from "axios";
import moment from "moment";
import React, { MouseEvent, useEffect, useState } from "react";
import { RouteComponentProps } from 'react-router-dom';
import Alert from "react-bootstrap/cjs/Alert";
import Button from "react-bootstrap/cjs/Button";
import Container from "react-bootstrap/cjs/Container";
import { extractFilenameFromContentDisposition } from "../../utils/toolkit";
import Data from "../../config/app.json";
import { StorageInfo } from "../../types/api-types";
import "./DownloadPage.css";

type DownloadPageRouterParams = {
    storageId: string;
};

interface DownloadPageProps extends RouteComponentProps<DownloadPageRouterParams> {

}

const DownloadPage = (props: DownloadPageProps) => {
    const [successMsg, setSuccessMsg] = useState("");
    const [errorMsg, setErrorMsg] = useState("");
    const [disableBtn, setDisableBtn] = useState(false);
    const [refreshToggle, setRefreshToggle] = useState(false);
    const [downloadInfo, setDownloadInfo] = useState<StorageInfo | null>(null);

    useEffect(() => {
        // retrieve information about the download
        axios.get(`${Data.api_endpoints.files_storage_info}/${props.match.params.storageId}`)
            .then(res => {
                if (res.status === 200) {
                    const info: StorageInfo = res.data;
                    setDownloadInfo(info);
                } else {
                    setErrorMsg("Download Link is Not Available!");
                }
            })
            .catch(err => {
                console.log(err);
                setErrorMsg("Download Link is no longer available!");
            })
        // eslint-disable-next-line
    }, [refreshToggle]);

    const handleDownload = (e: MouseEvent<HTMLButtonElement>) => {
        setSuccessMsg("");
        setDisableBtn(true);
        axios.get(`${Data.api_endpoints.download_files}/${props.match.params.storageId}`, {
            responseType: "blob"
        })
            .then(res => {
                const filename = extractFilenameFromContentDisposition(res.headers);
                const url = window.URL.createObjectURL(new Blob([res.data]));
                const link = document.createElement('a');
                link.href = url;
                link.setAttribute('download', filename);
                document.body.appendChild(link);
                link.click();
                setSuccessMsg("You have downloaded the files!");
                setDisableBtn(false);
                setRefreshToggle(!refreshToggle);
            })
            .catch(err => {
                console.log(err);
            });
    };

    return (
        <div className="download-container full-page">
            <Container>
                <h2>TempFileDrop.io</h2>
                {errorMsg ? (
                    <Alert variant="danger">
                        {errorMsg}
                    </Alert>
                ) : (
                    <div className="download-info-box">
                    {downloadInfo && (
                        <React.Fragment>
                            <p>Click on "Download" to retrieve your files</p>
                            <Button size="lg" onClick={handleDownload} disabled={disableBtn}>
                                Download
                            </Button>
                            <Alert variant="warning">
                                <strong>Please Note: </strong>Files will be deleted from our server after <strong>{downloadInfo.numOfDownloadsLeft}</strong> more downloads or after <strong>{moment(downloadInfo.expiryDatetime).format("DD MMM YYYY h:mma")}</strong>
                            </Alert>
                        </React.Fragment>
                    )}
                    </div>
                )}
                {successMsg && (
                    <Alert variant="success">
                        {successMsg}
                    </Alert>
                )}
            </Container>
        </div>
    );
};

export default DownloadPage;