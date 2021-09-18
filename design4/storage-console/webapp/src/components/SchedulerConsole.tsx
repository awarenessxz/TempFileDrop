import React, { MouseEvent, useEffect, useState } from "react";
import axios from "axios";
import moment, { Moment } from "moment";
import MomentUtils from "@date-io/moment";
import cronValidator from "cron-expression-validator";
import { createStyles, makeStyles, Theme } from "@material-ui/core/styles";
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
import LinearProgress from '@material-ui/core/LinearProgress';
import { DateTimePicker, MuiPickersUtilsProvider  } from "@material-ui/pickers";
import VerticallyCenteredModal from "./common/VerticallyCenteredModal";
import { useAuthState } from "../util/auth-context";
import { SchedulerJob, SchedulerJobStatus, SchedulerJobType, WatchListJob } from "../types/api-types";
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
        },
        linearProgressRoot: {
            width: '100%',
            '& > * + *': {
                marginTop: theme.spacing(2),
            },
            margin: theme.spacing(1),
        }
    }),
);

const SchedulerConsole = () => {
    const classes = useStyles();
    const { userToken } = useAuthState();
    const [errorMsg, setErrorMsg] = useState("");
    const [submitErrorMsg, setSubmitErrorMsg] = useState("");
    const [watchList, setWatchList] = useState<WatchListJob[]>([]);
    const [showModal, setShowModal] = useState(false);
    const [newWatchlist, setNewWatchList] = useState<SchedulerJob>({
        jobType: SchedulerJobType.MONITOR_OBJECT_CRONJOB,
        jobName: "",
        startTime: moment(),
        description: "",
        cronExpression: "0 */2 * ? * *"
    });
    // 0 0 * ? * * *
    const [newMonitorJobFormErrorState, setNewMonitorJobFormErrorState] = useState([false, false]);
    const [isSubmitting, setIsSubmitting] = useState(false);

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

    const handleSubmitCreateJob = (e: MouseEvent<HTMLButtonElement>) => {
        e.preventDefault();
        setSubmitErrorMsg("");
        setNewMonitorJobFormErrorState([false, false]);

        // validate
        const isCronValid = cronValidator.isValidCronExpression(newWatchlist.cronExpression);
        const isJobNameValid = newWatchlist.jobName.trim() !== "";
        if (isCronValid && isJobNameValid) {
            setIsSubmitting(true);
            console.log(newWatchlist);
            axios.post(`${Data.api_endpoints.scheduler}/create/${userToken?.username}`, newWatchlist)
                .then(res => console.log(res))
                .catch(err => console.error(err));
        } else {
            let message = "";
            const invalidIdx: number[] = [];
            if (!isCronValid) {
                message += "Invalid Cron Expression...";
                invalidIdx.push(1);
            }
            if (!isJobNameValid) {
                if (message !== "") {
                    message += " && "
                }
                message += "Please Enter Job Name...";
                invalidIdx.push(0);
            }
            setNewMonitorJobFormErrorState(newMonitorJobFormErrorState.map((state, idx) => invalidIdx.includes(idx)));
            setSubmitErrorMsg(message);
        }
    };

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
                                                        <Button variant="contained" color="primary" className={classes.button}>
                                                            Run Once
                                                        </Button>
                                                        {job.jobStatus === SchedulerJobStatus.PAUSED && (
                                                            <Button variant="contained" color="primary" className={classes.button}>
                                                                Resume
                                                            </Button>
                                                        )}
                                                        {(job.jobStatus !== SchedulerJobStatus.PAUSED && job.jobStatus !== SchedulerJobStatus.COMPLETED && job.jobStatus !== SchedulerJobStatus.COMPLETED_WITH_ERROR) && (
                                                            <Button variant="contained" color="primary" className={classes.button}>
                                                                Pause
                                                            </Button>
                                                        )}
                                                        <Button variant="contained" color="primary" className={classes.button}>
                                                            Edit
                                                        </Button>
                                                        <Button variant="contained" color="secondary" className={classes.button}>
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
                        {isSubmitting ? (
                            <div className={classes.linearProgressRoot}>
                                <LinearProgress />
                            </div>
                        ) : (
                            <React.Fragment>
                                <Grid item xs={12}>
                                    <TextField
                                        required
                                        fullWidth
                                        label="Job Name"
                                        value={newWatchlist.jobName}
                                        variant="outlined"
                                        onChange={(e) => setNewWatchList({ ...newWatchlist, jobName: e.target.value as string })}
                                        error={newMonitorJobFormErrorState[0]}
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
                                            {Object.keys(SchedulerJobType).map((value, idx) => <MenuItem key={idx} value={value}>{value}</MenuItem>)}
                                        </Select>
                                    </FormControl>
                                </Grid>
                                {newWatchlist.jobType === SchedulerJobType.MONITOR_OBJECT_CRONJOB && (
                                    <Grid item xs={12}>
                                        <TextField
                                            required
                                            fullWidth
                                            label="Cron Expression"
                                            helperText="Seconds Minutes Hours DayofMonth Month DayOfWeek Year"
                                            value={newWatchlist.cronExpression}
                                            variant="outlined"
                                            onChange={(e) => setNewWatchList({ ...newWatchlist, cronExpression: e.target.value as string })}
                                            error={newMonitorJobFormErrorState[1]}
                                        />
                                    </Grid>
                                )}
                                <Grid item xs={12}>
                                    <MuiPickersUtilsProvider libInstance={moment} utils={MomentUtils} locale="sg">
                                        <DateTimePicker
                                            label="Start Datetime"
                                            inputVariant="outlined"
                                            value={newWatchlist.startTime}
                                            onChange={(value) => setNewWatchList({ ...newWatchlist, startTime: value as Moment })}
                                            disablePast
                                            fullWidth
                                        />
                                    </MuiPickersUtilsProvider>
                                </Grid>
                                <Grid item xs={12}>
                                    <TextField
                                        fullWidth
                                        label="Description"
                                        value={newWatchlist.description}
                                        variant="outlined"
                                        onChange={(e) => setNewWatchList({ ...newWatchlist, description: e.target.value as string })}
                                        error={newMonitorJobFormErrorState[2]}
                                    />
                                </Grid>
                                {submitErrorMsg && (
                                    <Grid item xs={12}>
                                        <Alert severity="error">{submitErrorMsg}</Alert>
                                    </Grid>
                                )}
                                <Grid item xs={12} className={classes.alignRight}>
                                    <Button variant="contained" color="primary" onClick={handleSubmitCreateJob}>
                                        Create
                                    </Button>
                                </Grid>
                            </React.Fragment>
                        )}
                    </Grid>
                )}
            />
        </div>
    )
};

export default SchedulerConsole;