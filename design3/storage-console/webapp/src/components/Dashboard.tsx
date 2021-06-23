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
import { EventData, StorageInfo } from "../types/api-types";
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
        label: {
            fontWeight: 'bold',
            marginBottom: '10px',
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
    const [messageQueues, setMessageQueues] = useState<string[]>([]);
    const [subscribers, setSubscribers] = useState<string[]>([]);
    const [eventDataList, setEventDataList] = useState<EventData[]>([]);

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
            if (userToken.isAdmin) {
                axios.get(`${Data.api_endpoints.storagesvc}/admin/storage-info`)
                    .then(res => {
                        const storageInfoList: StorageInfo[] = res.data;
                        setBuckets(storageInfoList.map((info) => info.bucket));
                        setNumOfFiles(storageInfoList.length);
                    })
                    .catch(err => {
                        console.error(err);
                    });
                axios.get(`${Data.api_endpoints.storagesvc}/admin/events`)
                    .then(res => {
                        const eventDataList: EventData[] = res.data;
                        setEventDataList(eventDataList.sort(compareFn));
                    })
                    .catch(err => {
                        console.error(err);
                    });
            } else {
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
                setMessageQueues(userToken.routingKeys.map((key) => `${Data.exchangeName}.${key}`));
                setSubscribers(userToken.subscribers);
            }
        }
    }, [userToken]);

    return (
        <div>
            <h2 className={classes.header}>Dashboard</h2>
            <Grid container spacing={2} className={classes.gridContainer}>
                <Grid item xs={2}>
                    <Paper className={classes.paper}>
                        <Typography className={classes.label}>Number of Files</Typography>
                        <Typography>{numOfFiles}</Typography>
                    </Paper>
                </Grid>
                <Grid item xs={3}>
                    <Paper className={classes.paper}>
                        <Typography className={classes.label}>Buckets</Typography>
                        {buckets.map((bucket, idx) => <Typography key={idx}>{bucket}</Typography> )}
                        {buckets.length === 0 && (
                            <Alert severity="warning">No Buckets Found</Alert>
                        )}
                    </Paper>
                </Grid>
                {!userToken?.isAdmin && (
                    <React.Fragment>
                        <Grid item xs={3}>
                            <Paper className={classes.paper}>
                                <Typography className={classes.label}>Message Queues</Typography>
                                {messageQueues.map((queue, idx) => <Typography key={idx}>{queue}</Typography> )}
                                {messageQueues.length === 0 && (
                                    <Alert severity="warning">No Queues Found</Alert>
                                )}
                            </Paper>
                        </Grid>
                        <Grid item xs={3}>
                            <Paper className={classes.paper}>
                                <Typography className={classes.label}>Message Queue Subscribers</Typography>
                                {subscribers.map((sub, idx) => <Typography key={idx}>{sub}</Typography> )}
                                {subscribers.length === 0 && (
                                    <Alert severity="warning">No Subscribers Found</Alert>
                                )}
                            </Paper>
                        </Grid>
                    </React.Fragment>
                )}
            </Grid>
            <Paper className={classes.paper}>
                <Grid container spacing={2}>
                    <Grid item xs={12}>
                        <h2 className={classes.header}>Events</h2>
                    </Grid>
                    <Grid item xs={12}>
                        {eventDataList.length > 0 ? (
                            <TableContainer className={classes.tableContainer}>
                                <Table aria-label="simple table" stickyHeader>
                                    <TableHead>
                                        <TableRow>
                                            <TableCell>Event Type</TableCell>
                                            <TableCell>Bucket</TableCell>
                                            <TableCell>Publish Datetime</TableCell>
                                            <TableCell>StorageId</TableCell>
                                            <TableCell>Files</TableCell>
                                        </TableRow>
                                    </TableHead>
                                    <TableBody>
                                        {eventDataList.map((eventData) => (
                                            <TableRow key={eventData.storageId}>
                                                <TableCell>{eventData.eventType}</TableCell>
                                                <TableCell>{eventData.bucket}</TableCell>
                                                <TableCell>{moment(eventData.publishedDateTime).format("DD MMM YYYY h:mma")}</TableCell>
                                                <TableCell>{eventData.storageId}</TableCell>
                                                <TableCell>{eventData.storageFiles}</TableCell>
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
