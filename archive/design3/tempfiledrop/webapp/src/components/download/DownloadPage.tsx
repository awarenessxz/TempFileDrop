import moment from "moment";
import React, { MouseEvent, useEffect, useState } from "react";
import { RouteComponentProps } from 'react-router-dom';
import StorageClient, { DownloadInfo } from "storage-js-client";
import Alert from "react-bootstrap/cjs/Alert";
import Button from "react-bootstrap/cjs/Button";
import Container from "react-bootstrap/cjs/Container";
import Data from "../../config/app.json";
import { useAuthState } from "../../utils/auth-context";
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
    const [downloadInfo, setDownloadInfo] = useState<DownloadInfo | null>(null);

    useEffect(() => {
        if (isAuthReady) {
            // retrieve information about the download
            StorageClient.getDownloadLink({
                url: `${Data.api_endpoints.storagesvc_get_downloadlink}/${props.match.params.storageId}`,
                onSuccess: (downloadInfo: DownloadInfo) => {
                    setDownloadInfo(downloadInfo);
                },
                onError: (err) => {
                    console.log(err);
                    setErrorMsg("Download Link is no longer available!");
                }
            });
        }
        // eslint-disable-next-line
    }, [refreshToggle, isAuthenticated, isAuthReady]);

    const handleDownload = (e: MouseEvent<HTMLButtonElement>) => {
        setSuccessMsg("");
        setDisableBtn(true);
        if (downloadInfo !== null) {
            const token = window.accessToken ? window.accessToken : "dummy_token";
            StorageClient.download({
                url: downloadInfo.downloadEndpoint,
                eventRoutingKey: isAuthenticated ? Data.rabbitmq.routingkey : "",
                headers: downloadInfo.requiresAuthentication ? { 'Authorization': 'Bearer ' + token } : {},
                onError(err: any): void {
                    console.log(err);
                    if (err.response && err.response.status === 401) {
                        setErrorMsg("Authentication Required!");
                    }
                },
                onSuccess(): void {
                    setSuccessMsg("You have downloaded the files!");
                    setDisableBtn(false);
                    setRefreshToggle(!refreshToggle);
                }
            });
        } else {
            setErrorMsg("Download Failed!");
        }
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
                    {(downloadInfo && downloadInfo.requiresAuthentication && !isAuthenticated) ? (
                        <Alert variant="danger">
                            <strong>Authentication Required to download files</strong>
                        </Alert>
                    ): (
                      <React.Fragment>
                          <p>Click on "Download" to retrieve your files</p>
                          <Button size="lg" onClick={handleDownload} disabled={disableBtn}>
                              Download
                          </Button>
                          <Alert variant="warning">
                              <strong>Please Note: </strong>Files will be deleted from our server after <strong>{downloadInfo?.numOfDownloadsLeft}</strong> more downloads or after <strong>{moment(downloadInfo?.expiryDatetime).format("DD MMM YYYY h:mma")}</strong>
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