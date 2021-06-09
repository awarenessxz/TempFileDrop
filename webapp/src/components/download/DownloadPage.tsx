import axios from "axios";
import moment from "moment";
import React, { MouseEvent, useEffect, useState } from "react";
import { RouteComponentProps } from 'react-router-dom';
import Alert from "react-bootstrap/cjs/Alert";
import Button from "react-bootstrap/cjs/Button";
import Container from "react-bootstrap/cjs/Container";
import { extractFilenameFromContentDisposition } from "../../utils/toolkit";
import Data from "../../config/app.json";
import { useAuthState } from "../../utils/auth-context";
import { StorageInfo } from "../../types/api-types";
import "./DownloadPage.css";

type DownloadPageRouterParams = {
    storageId: string;
};

interface DownloadPageProps extends RouteComponentProps<DownloadPageRouterParams> {

}

const DownloadPage = (props: DownloadPageProps) => {
    const { isAuthReady, isAuthenticated } = useAuthState();
    const [successMsg, setSuccessMsg] = useState("");
    const [errorMsg, setErrorMsg] = useState("");
    const [disableBtn, setDisableBtn] = useState(false);
    const [refreshToggle, setRefreshToggle] = useState(false);
    const [downloadInfo, setDownloadInfo] = useState<StorageInfo | null>(null);

    useEffect(() => {
        if (isAuthReady) {
            // retrieve information about the download
            const url = isAuthenticated
                ? `${Data.api_endpoints.storagesvc_storage_info}/${Data.bucket}/${props.match.params.storageId}`
                : `${Data.api_endpoints.storagesvc_storage_info_anonymous}/${props.match.params.storageId}`;

            axios.get(url)
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
                });
        }
        // eslint-disable-next-line
    }, [refreshToggle, isAuthenticated, isAuthReady]);

    const handleDownload = (e: MouseEvent<HTMLButtonElement>) => {
        setSuccessMsg("");
        setDisableBtn(true);
        const url = isAuthenticated
            ? `${Data.api_endpoints.storagesvc_download}/${Data.bucket}/${props.match.params.storageId}`
            : `${Data.api_endpoints.storagesvc_download_anonymous}/${props.match.params.storageId}`;
        axios.get(url, {
            responseType: "blob",
            params: isAuthenticated ? { eventRoutingKey: Data.rabbitmq.routingkey } : {}
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