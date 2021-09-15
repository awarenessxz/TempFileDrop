import React, { useEffect, useState } from "react";
import axios from "axios";
import moment from "moment";
import { makeStyles, createStyles, Theme } from '@material-ui/core/styles';
import Alert from '@material-ui/lab/Alert';
import Paper from '@material-ui/core/Paper';
import Grid from '@material-ui/core/Grid';
import IconButton from "@material-ui/core/IconButton";
import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableContainer from '@material-ui/core/TableContainer';
import TableHead from '@material-ui/core/TableHead';
import TableRow from '@material-ui/core/TableRow';
import DescriptionIcon from '@material-ui/icons/Description';
import FolderIcon from '@material-ui/icons/Folder';
import DeleteForever from '@material-ui/icons/DeleteForever';
import BreadcrumbsBar from "./common/BreadcrumbsBar";
import { useAuthState } from "../util/auth-context";
import { humanFileSize } from "../util/file-util";
import { FileSystemNode } from "../types/api-types";
import Data from "../config/app.json";

const useStyles = makeStyles((theme: Theme) =>
    createStyles({
        root: {
            flexGrow: 1,
        },
        header: {
            margin: 0,
            textAlign: "left",
            color: "#fc8608",
        },
        paper: {
            padding: theme.spacing(2),
            textAlign: 'center',
            color: theme.palette.text.secondary,
        },
        cellIcon: {
            padding: '16px 0 16px 16px',
        },
        cellAnchor: {
            cursor: 'pointer',
            '&:hover': {
                color: 'green',
                textDecoration: 'underline',
            },
        },
    }),
);

const BucketConsole = () => {
    const classes = useStyles();
    const { userToken } = useAuthState();
    const [errorMsg, setErrorMsg] = useState("");
    const [rootDir, setRootDir] = useState<FileSystemNode|null>(null);
    const [currentDir, setCurrentDir] = useState<FileSystemNode|null>(null);

    const createFileSystemTree = (bucketFileSystemNodes: FileSystemNode[]) => {
        const rootDir: FileSystemNode = {
            children: [],
            label: "ROOT",
            storageFullPath: "Root",
            storageSize: 0,
            storageBucket: "",
            storageExpiryDatetime: null,
            storageDownloadLeft: 0,
            isFile: false
        };
        bucketFileSystemNodes.forEach(bucketFIleSystemNode => rootDir.children.push(bucketFIleSystemNode));
        setCurrentDir(rootDir);
        setRootDir(rootDir);
    };

    useEffect(() => {
        if (userToken) {
            if (userToken.isAdmin) {
                axios.get(`${Data.api_endpoints.storagesvc}/list`)
                    .then(response => {
                        const bucketFileSystemNodes: FileSystemNode[] = response.data;
                        createFileSystemTree(bucketFileSystemNodes);
                    })
                    .catch(err =>{
                        console.error(err);
                        setErrorMsg("Fail to retrieve storage information from storage service!");
                    })
            } else {
                const requests = userToken.buckets.map(bucket => axios.get(`${Data.api_endpoints.storagesvc}/list/${bucket}`));
                Promise.all(requests)
                    .then(results => {
                        const bucketFileSystemNodes = results.map(response => response.data);
                        createFileSystemTree(bucketFileSystemNodes);
                    })
                    .catch(err => {
                        console.error(err);
                        setErrorMsg("Fail to retrieve storage information from storage service!");
                    });
            }
        }
    }, [userToken]);

    const handleDelete = (parent: FileSystemNode, childIdx: number, node: FileSystemNode) => {
        axios.delete(`${Data.api_endpoints.storagesvc}/${node.storageBucket}`)
            .then(res => {
                if (res.status === 200) {
                    parent.children.splice(childIdx, 1);
                    setCurrentDir({...parent});
                    // Note: RootDirectory is not updated...
                }
            })
            .catch(err => {
                console.error(err);
                setErrorMsg("Fail to delete storage - " + node.label);
            });
    };

    const handleBreadcrumbClick = (path: string) => {
        const paths = path.substring(1).split('/');
        let node = rootDir;
        paths.forEach(label => {
            const tempNode = node?.children.find(element => element.label === label);
            if (tempNode) {
                node = tempNode;
            }
        });
        setCurrentDir(node);
    };

    const getBreadcrumbPath = (): string => {
        const path = currentDir ? currentDir.storageFullPath : "";
        if (currentDir?.label !== "ROOT") {
            return `Root${path}`;
        }
        return path;
    };

    return (
        <Paper className={`${classes.root} ${classes.paper}`}>
            <Grid container spacing={3}>
                <Grid item xs={12}>
                    <h2 className={classes.header}>Your Buckets</h2>
                </Grid>
                {errorMsg ? (
                    <Grid item xs={12}>
                        <Alert severity="error">{errorMsg}</Alert>
                    </Grid>
                ) : (
                    <React.Fragment>
                        <Grid item xs={12}>
                            <BreadcrumbsBar callback={handleBreadcrumbClick} path={getBreadcrumbPath()} />
                        </Grid>
                        <Grid item xs={12}>
                            {currentDir && (
                                <TableContainer>
                                    <Table aria-label="simple table">
                                        <TableHead>
                                            <TableRow>
                                                <TableCell>Name</TableCell>
                                                <TableCell/>
                                                <TableCell align="center">Size</TableCell>
                                                <TableCell align="center">Downloads Left</TableCell>
                                                <TableCell align="center">Expiry Datetime</TableCell>
                                                <TableCell align="center">Actions</TableCell>
                                            </TableRow>
                                        </TableHead>
                                        <TableBody>
                                            {currentDir.children.length > 0 && currentDir.children.map((fileSystemNode, idx) => (
                                                <TableRow key={idx}>
                                                    <TableCell width={'1vw'} className={classes.cellIcon}>
                                                        {fileSystemNode.isFile ? <DescriptionIcon /> : <FolderIcon />}
                                                    </TableCell>
                                                    <TableCell
                                                        component="th"
                                                        scope="row"
                                                        onClick={() => {
                                                            if (!fileSystemNode.isFile) {
                                                                setCurrentDir(fileSystemNode);
                                                            }
                                                        }}
                                                        className={!fileSystemNode.isFile ? classes.cellAnchor: undefined}
                                                    >
                                                        {fileSystemNode.label}
                                                    </TableCell>
                                                    <TableCell align="center">{fileSystemNode.isFile && humanFileSize(fileSystemNode.storageSize, false, 2)}</TableCell>
                                                    <TableCell align="center">{fileSystemNode.isFile && fileSystemNode.storageDownloadLeft}</TableCell>
                                                    <TableCell align="center">{fileSystemNode.isFile && moment(fileSystemNode.storageExpiryDatetime).format("DD MMM YYYY h:mma")}</TableCell>
                                                    <TableCell align="center">
                                                        {fileSystemNode.isFile && (
                                                            <IconButton onClick={() => handleDelete(currentDir, idx, fileSystemNode)}>
                                                                <DeleteForever color="error" />
                                                            </IconButton>
                                                        )}
                                                    </TableCell>
                                                </TableRow>
                                            ))}
                                        </TableBody>
                                    </Table>
                                </TableContainer>
                            )}
                        </Grid>
                    </React.Fragment>
                )}
            </Grid>
        </Paper>
    );
};

export default BucketConsole;
