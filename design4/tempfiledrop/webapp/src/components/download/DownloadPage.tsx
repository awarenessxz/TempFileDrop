import moment from "moment";
import React, { MouseEvent, useEffect, useState } from "react";
import { RouteComponentProps } from 'react-router-dom';
import StorageClient, { StorageMetadata, StorageMetadataResponse } from "storage-js-client";
import Alert from "react-bootstrap/cjs/Alert";
import Button from "react-bootstrap/cjs/Button";
import Container from "react-bootstrap/cjs/Container";
import { useAuthState } from "../../utils/auth-context";
import "./DownloadPage.css";

type DownloadPageRouterParams = {
    objectName: string;
};

interface DownloadPageProps extends RouteComponentProps<DownloadPageRouterParams> {

}

const DownloadPage = (props: DownloadPageProps) => {
    const { isAuthReady, isAuthenticated } = useAuthState();
    const [successMsg, setSuccessMsg] = useState("");
    const [errorMsg, setErrorMsg] = useState("");
    const [disableBtn, setDisableBtn] = useState(true);
    const [refreshToggle, setRefreshToggle] = useState(false);
    const [storageMetadata, setStorageMetadata] = useState<StorageMetadata | null>(null);

    useEffect(() => {
        if (isAuthReady) {
            // retrieve information about the download
            const objectName = props.match.params.objectName;
            StorageClient.getStorageMetadata({
                storageObjects: [objectName],
                onSuccess: (res: StorageMetadataResponse) => {
                    if (res.storageMetadataList.hasOwnProperty(objectName)) {
                        const metadata = res.storageMetadataList[objectName];
                        setStorageMetadata(metadata);
                        setDisableBtn(false);
                    } else {
                        setErrorMsg("Download Link is no longer available");
                    }
                },
                onError: (err) => {
                    console.error(err);
                    setErrorMsg("Download Link is no longer available");
                }
            });
        }
        // eslint-disable-next-line
    }, [refreshToggle, isAuthenticated, isAuthReady]);

    const handleDownload = (e: MouseEvent<HTMLButtonElement>) => {
        setSuccessMsg("");
        setDisableBtn(true);
        if (storageMetadata !== null) {
            const token = window.accessToken ? window.accessToken : "dummy_token";
            StorageClient.downloadViaPresignedUrl({
                metadata: {
                    bucket: storageMetadata.bucket,
                    storageObjects: [storageMetadata.objectName]
                },
                headers: isAuthenticated ? { 'Authorization': 'Bearer ' + token } : {},
                onError: (err: any) => {
                    console.log(err);
                    if (err.response && err.response.status === 401) {
                        setErrorMsg("Authentication Required!");
                    }
                },
                onSuccess: () => {
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
                    {(storageMetadata && !isAuthenticated) ? (
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
                              <strong>Please Note: </strong>Files will be deleted from our server after <strong>{storageMetadata?.numOfDownloadsLeft}</strong> more downloads or after <strong>{moment(storageMetadata?.expiryDatetime).format("DD MMM YYYY h:mma")}</strong>
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