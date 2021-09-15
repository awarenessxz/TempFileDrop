import React, { useEffect, useState } from "react";
import axios from "axios";
import moment from "moment";
import { createStyles, makeStyles, Theme  } from "@material-ui/core/styles";
import Alert from "@material-ui/lab/Alert";
import Paper from "@material-ui/core/Paper";
import Grid from "@material-ui/core/Grid";
import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableContainer from '@material-ui/core/TableContainer';
import TableHead from '@material-ui/core/TableHead';
import TableRow from '@material-ui/core/TableRow';
import Typography from '@material-ui/core/Typography';
import { useAuthState } from "../util/auth-context";
import { EventData } from "../types/api-types";
import Data from "../config/app.json";

const useStyles = makeStyles((theme: Theme) =>
    createStyles({
        header: {
            margin: 0,
            textAlign: "left",
            color: "#fc8608",
        },
        paper: {
            flexGrow: 1,
            padding: theme.spacing(2),
            textAlign: 'center',
            color: theme.palette.text.secondary,
            margin: '10px 0',
            height: '100%',
        },
        gridContainer: {
            marginBottom: '20px',
        },
        tableContainer: {
            maxHeight: '560px',
        }
    }),
);

const Dashboard = () => {
    const classes = useStyles();
    const { userToken } = useAuthState();
    const [numOfFiles, setNumOfFiles] = useState(0);
    const [buckets, setBuckets] = useState<string[]>([]);
    const [eventDataList, setEventDataList] = useState<EventData[]>([]);
    const [watchList, setWatchList] = useState<string[]>([]);

    const compareFn = (a: EventData, b: EventData) => {
        if (moment(a.publishedDateTime).isAfter(moment(b.publishedDateTime))) {
            return -1;
        } else if (moment(a.publishedDateTime).isBefore(moment(b.publishedDateTime))) {
            return 1;
        }
        return 0;
    };

    useEffect(() => {
        if (userToken) {
            const requests = userToken.buckets.map(bucket => axios.get(`${Data.api_endpoints.storagesvc}/events/${bucket}`));
            Promise.all(requests)
                .then(results => {
                    let allEventDataList: EventData[] = [];
                    results.forEach(response => {
                        const eventDataList: EventData[] = response.data;
                        allEventDataList = [...allEventDataList, ...eventDataList];
                    });
                    setEventDataList(allEventDataList.sort(compareFn));
                })
                .catch(err => {
                    console.error(err);
                });
            setBuckets(userToken.buckets);
        }
    }, [userToken]);

    return (
        <div>
            <h2 className={classes.header}>Dashboard</h2>
            <Grid container spacing={2} className={classes.gridContainer}>
                <Grid item xs={4}>
                    <Paper className={classes.paper}>
                        <h3 className={classes.header}>Number of Files</h3>
                        <Typography>{numOfFiles}</Typography>
                    </Paper>
                </Grid>
                <Grid item xs={4}>
                    <Paper className={classes.paper}>
                        <h3 className={classes.header}>Buckets</h3>
                        {buckets.map((bucket, idx) => <Typography key={idx}>{bucket}</Typography> )}
                        {buckets.length === 0 && (
                            <Alert severity="warning">No Buckets Found</Alert>
                        )}
                    </Paper>
                </Grid>
            </Grid>
            <Paper className={classes.paper}>
                <Grid container spacing={2}>
                    <Grid item xs={12}>
                        <h3 className={classes.header}>Watch List</h3>
                    </Grid>
                    <Grid item xs={12}>
                        {watchList.length > 0 ? (
                            <Alert severity="warning">No Events Found</Alert>
                        ): (
                            <Alert severity="warning">No objects being monitored</Alert>
                        )}
                    </Grid>
                </Grid>
            </Paper>
            <Paper className={classes.paper}>
                <Grid container spacing={2}>
                    <Grid item xs={12}>
                        <h3 className={classes.header}>Events</h3>
                    </Grid>
                    <Grid item xs={12}>
                        {eventDataList.length > 0 ? (
                            <TableContainer className={classes.tableContainer}>
                                <Table aria-label="simple table" stickyHeader>
                                    <TableHead>
                                        <TableRow>
                                            <TableCell>Event Type</TableCell>
                                            <TableCell>Bucket</TableCell>
                                            <TableCell>Object</TableCell>
                                            <TableCell>Publish Datetime</TableCell>
                                        </TableRow>
                                    </TableHead>
                                    <TableBody>
                                        {eventDataList.map((eventData, idx) => (
                                            <TableRow key={idx}>
                                                <TableCell>{eventData.eventType}</TableCell>
                                                <TableCell>{eventData.bucket}</TableCell>
                                                <TableCell>{eventData.objectName}</TableCell>
                                                <TableCell>{moment(eventData.publishedDateTime).format("DD MMM YYYY h:mma")}</TableCell>
                                            </TableRow>
                                        ))}
                                    </TableBody>
                                </Table>
                            </TableContainer>
                        ): (
                            <Alert severity="warning">No Events Found</Alert>
                        )}
                    </Grid>
                </Grid>
            </Paper>
        </div>
    )
};

export default Dashboard;
