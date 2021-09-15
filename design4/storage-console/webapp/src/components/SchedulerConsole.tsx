import React, {useEffect, useState} from "react";
import axios from "axios";
import {createStyles, makeStyles, Theme} from "@material-ui/core/styles";
import AddAlarmIcon from '@material-ui/icons/AddAlarm';
import Button from "@material-ui/core/Button";
import Paper from "@material-ui/core/Paper";
import Grid from "@material-ui/core/Grid";
import Alert from "@material-ui/lab/Alert";
import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableContainer from '@material-ui/core/TableContainer';
import TableHead from '@material-ui/core/TableHead';
import TableRow from '@material-ui/core/TableRow';
import TextField from '@material-ui/core/TextField';
import InputLabel from '@material-ui/core/InputLabel';
import MenuItem from '@material-ui/core/MenuItem';
import FormControl from '@material-ui/core/FormControl';
import Select from '@material-ui/core/Select';
import VerticallyCenteredModal from "./common/VerticallyCenteredModal";
import {useAuthState} from "../util/auth-context";
import {SchedulerJob, SchedulerJobStatus, SchedulerJobType, WatchListJob} from "../types/api-types";
import Data from "../config/app.json";

const useStyles = makeStyles((theme: Theme) =>
    createStyles({
        root: {
            flexGrow: 1,
        },
        button: {
            margin: theme.spacing(1),
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
        alignRight: {
            textAlign: 'right'
        },
        alignCenterVertical: {
            alignItems: 'center'
        },
        formControl: {
            width: '100%',
        }
    }),
);

const SchedulerConsole = () => {
    const classes = useStyles();
    const { userToken } = useAuthState();
    const [errorMsg, setErrorMsg] = useState("");
    const [watchList, setWatchList] = useState<WatchListJob[]>([]);
    const [showModal, setShowModal] = useState(false);
    const [newWatchlist, setNewWatchList] = useState<SchedulerJob>({
        jobType: SchedulerJobType.MONITOR_OBJECT_CRONJOB,
        jobName: "",
        description: "",
        cronExpression: "* * * * *"
    });

    useEffect(() => {
        if (userToken) {
            axios.get(`${Data.api_endpoints.scheduler}/list/${userToken.username}`)
                .then(res => {
                    const result: WatchListJob[] = res.data;
                    setWatchList(result);
                })
                .catch(err => {
                    setErrorMsg("Fail to retrieve watchlist...");
                });
        }
    }, [userToken]);

    return (
        <div>
            <Paper className={`${classes.root} ${classes.paper}`}>
                <Grid container spacing={3}>
                    <Grid item container xs={12} className={classes.alignCenterVertical}>
                        <Grid item xs={4}>
                            <h2 className={classes.header}>Your Watchlist</h2>
                        </Grid>
                        <Grid item xs={8} className={classes.alignRight}>
                            <Button variant="contained" color="primary" className={classes.button} endIcon={<AddAlarmIcon />} onClick={() => setShowModal(true)}>
                                Monitor New Object
                            </Button>
                        </Grid>
                    </Grid>
                    {errorMsg ? (
                        <Grid item xs={12}>
                            <Alert severity="error">{errorMsg}</Alert>
                        </Grid>
                    ) : (
                        <Grid item container xs={12}>
                            <TableContainer>
                                {watchList.length >= 0 ? (
                                    <Table aria-label="simple table">
                                        <TableHead>
                                            <TableRow>
                                                <TableCell>No</TableCell>
                                                <TableCell>Name</TableCell>
                                                <TableCell>Type</TableCell>
                                                <TableCell>Crons</TableCell>
                                                <TableCell>Status</TableCell>
                                                <TableCell>Is Object Valid?</TableCell>
                                                <TableCell>Description</TableCell>
                                                <TableCell>Actions</TableCell>
                                            </TableRow>
                                        </TableHead>
                                        <TableBody>
                                            {watchList.map((job, idx) => (
                                                <TableRow key={idx}>
                                                    <TableCell>{idx+1}</TableCell>
                                                    <TableCell>{job.jobName}</TableCell>
                                                    <TableCell>{job.jobType}</TableCell>
                                                    <TableCell>{job.cronExpression}</TableCell>
                                                    <TableCell>{job.jobStatus}</TableCell>
                                                    <TableCell>{job.isObjectValid ? "VALID" : "INVALID"}</TableCell>
                                                    <TableCell>{job.description}</TableCell>
                                                    <TableCell>
                                                        <Button variant="contained" color="primary">
                                                            Run Once
                                                        </Button>
                                                        {job.jobStatus === SchedulerJobStatus.PAUSED && (
                                                            <Button variant="contained" color="primary">
                                                                Resume
                                                            </Button>
                                                        )}
                                                        {(job.jobStatus !== SchedulerJobStatus.PAUSED && job.jobStatus !== SchedulerJobStatus.COMPLETED && job.jobStatus !== SchedulerJobStatus.COMPLETED_WITH_ERROR) && (
                                                            <Button variant="contained" color="primary">
                                                                Pause
                                                            </Button>
                                                        )}
                                                        <Button variant="contained" color="primary">
                                                            Edit
                                                        </Button>
                                                        <Button variant="contained" color="secondary">
                                                            Delete
                                                        </Button>
                                                    </TableCell>
                                                </TableRow>
                                            ))}
                                        </TableBody>
                                    </Table>
                                ) : (
                                    <Alert severity="warning">No objects being monitored</Alert>
                                )}
                            </TableContainer>
                        </Grid>
                    )}
                </Grid>
            </Paper>
            <VerticallyCenteredModal
                title="Monitor New Object"
                width={800}
                show={showModal}
                onClose={() => setShowModal(!showModal)}
                content={(
                    <Grid container spacing={3}>
                        <Grid item xs={12}>
                            <TextField
                                required
                                fullWidth
                                label="Job Name"
                                value={newWatchlist.jobName}
                                variant="outlined"
                                onChange={(e) => setNewWatchList({ ...newWatchlist, jobName: e.target.value as string })}
                            />
                        </Grid>
                        <Grid item xs={12}>
                            <FormControl variant="outlined" className={classes.formControl}>
                                <InputLabel id="jobtype-select">Job Type</InputLabel>
                                <Select
                                    labelId="jobtype-select"
                                    label="Job Type"
                                    value={newWatchlist.jobType}
                                    onChange={(e) => setNewWatchList({ ...newWatchlist, jobType: e.target.value as SchedulerJobType })}
                                >
                                    {Object.keys(SchedulerJobType).map((value) => <MenuItem value={value}>{value}</MenuItem>)}
                                </Select>
                            </FormControl>
                        </Grid>
                        {newWatchlist.jobType === SchedulerJobType.MONITOR_OBJECT_CRONJOB && (
                            <Grid item xs={12}>
                                <TextField
                                    required
                                    fullWidth
                                    label="Cron Expression"
                                    value={newWatchlist.description}
                                    variant="outlined"
                                    onChange={(e) => setNewWatchList({ ...newWatchlist, cronExpression: e.target.value as string })}
                                />
                            </Grid>
                        )}
                        <Grid item xs={12}>
                            <TextField
                                required
                                fullWidth
                                label="Description"
                                value={newWatchlist.description}
                                variant="outlined"
                                onChange={(e) => setNewWatchList({ ...newWatchlist, description: e.target.value as string })}
                            />
                        </Grid>
                        <Grid item xs={12} className={classes.alignRight}>
                            <Button variant="contained" color="primary">
                                Create
                            </Button>
                        </Grid>
                    </Grid>
                )}
            />
        </div>
    )
};

export default SchedulerConsole;