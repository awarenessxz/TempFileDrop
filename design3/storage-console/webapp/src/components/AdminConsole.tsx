import React, { useState } from "react";
import { createStyles, makeStyles, Theme  } from "@material-ui/core/styles";
import Accordion from '@material-ui/core/Accordion';
import AccordionDetails from '@material-ui/core/AccordionDetails';
import AccordionSummary from '@material-ui/core/AccordionSummary';
import Button from '@material-ui/core/Button';
import Typography from '@material-ui/core/Typography';
import ExpandMoreIcon from '@material-ui/icons/ExpandMore';
import Paper from "@material-ui/core/Paper";
import Grid from "@material-ui/core/Grid";
import Box from '@material-ui/core/Box';
import TextField from '@material-ui/core/TextField';
import AddIcon from '@material-ui/icons/Add';

interface RouterKeyState {
    routerKey: string;
    messageQueue: string;
}

interface SubscriberState {
    account: string;
    password: string;
    routerKey: string;
}

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
        groupHeader: {
            backgroundColor: 'rgba(0, 0, 0, .09)',
        },
        form: {
            padding: '20px 0',
            width: '100%',
            textAlign: 'left',
        },
        formTextField: {
            margin: '10px',
            padding: '10px 5px',
        },
        formLabel: {
            margin: '20px 0 0 0',
            fontWeight: 'bold',
        },
        formText: {
            margin: '5px 10px 0',
        },
        routerBorder: {
            margin: '5px 5px',
            padding: '10px',
            border: '1px dotted black',
        },
        button: {
            margin: '5px',
        },
    }),
);

const AdminConsole = () => {
    const classes = useStyles();
    const [expanded, setExpanded] = useState("panel1");
    const [serviceName, setServiceName] = useState("");
    const [routerKeys, setRouterKeys] = useState<RouterKeyState[]>([{ routerKey: "", messageQueue: "" }]);

    const renderRouterKeyRow = (state: RouterKeyState, idx: number) => {
        return (
            <Grid container className={classes.routerBorder}>
                <Grid item xs={3}>
                    <TextField
                        required
                        fullWidth
                        label="Routing Key"
                        value={state.routerKey}
                        onChange={(e) => {
                            const updatedState = [...routerKeys];
                            const newValue = e.target.value;
                            updatedState[idx] = {
                                routerKey: newValue,
                                messageQueue: newValue ? `Exchange.${newValue}` : ""
                            };
                            setRouterKeys(updatedState);
                        }}
                        className={classes.formTextField}
                    />
                </Grid>
                <Grid item xs={9}>
                    <TextField
                        disabled
                        fullWidth
                        label="Message Queue"
                        variant="filled"
                        value={state.messageQueue}
                        className={classes.formTextField}
                    />
                </Grid>
            </Grid>
        );
    };

    const renderSubscriberRow = (state: RouterKeyState, idx: number) => {

    };

    const handleAddRouterKeyRow = () => {
        setRouterKeys([...routerKeys, { routerKey: "", messageQueue: "" }]);
    };

    const renderCreateServiceTab = (panel: string) => {
        return (
            <Accordion expanded={expanded === panel} onChange={() => setExpanded(panel)}>
                <AccordionSummary className={classes.groupHeader} aria-controls="panel1d-content" id="panel1d-header" expandIcon={<ExpandMoreIcon />}>
                    <Typography>Create New Service</Typography>
                </AccordionSummary>
                <AccordionDetails>
                    <Box className={classes.form}>
                        <Typography align="left">Fill in the details below to create a new service for consuming centralized storage service.</Typography>
                        <Typography align="left" variant="h5" className={classes.formLabel}>Storage</Typography>
                        <Typography align="left" className={classes.formText}>Each service will be tagged to a dedicated bucket for storage</Typography>
                        <TextField
                            required
                            fullWidth
                            label="Service Name"
                            onChange={(e) => setServiceName(e.target.value)}
                            className={classes.formTextField}
                        />
                        <TextField
                            disabled
                            fullWidth
                            label="Bucket"
                            variant="filled"
                            value={serviceName}
                            className={classes.formTextField}
                        />
                        <Typography align="left" variant="h5" className={classes.formLabel}>Message Queue</Typography>
                        <Typography align="left" className={classes.formText}>
                            Each Message Queue will be tagged to a router key. Uploaded/Deleted/Downloaded events will be
                            published to the message queue by Storage Service based on router key provided by the service.
                        </Typography>
                        {routerKeys.map((state, idx) => renderRouterKeyRow(state, idx))}
                        <Button variant="contained" color="primary" startIcon={<AddIcon />} className={classes.button} onClick={handleAddRouterKeyRow}>
                            Add Router Keys
                        </Button>
                        <Typography align="left" variant="h5" className={classes.formLabel}>Message Queue Subscribers</Typography>



                        <Button variant="contained" color="secondary" className={classes.button} onClick={handleAddRouterKeyRow}>
                            Create
                        </Button>
                    </Box>
                </AccordionDetails>
            </Accordion>
        );
    };

    return (
        <Paper className={`${classes.root} ${classes.paper}`}>
            <Grid container spacing={3}>
                <Grid item xs={12}>
                    <h2 className={classes.header}>Admin Console</h2>
                </Grid>
                <Grid item xs={12}>
                    {renderCreateServiceTab("panel1")}
                </Grid>
            </Grid>
        </Paper>
    )
};

export default AdminConsole;
