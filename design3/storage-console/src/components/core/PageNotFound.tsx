import React from 'react';
import { createStyles, makeStyles } from '@material-ui/core/styles';
import Error from "../../assets/error.jpg";

const useStyles = makeStyles(() =>
    createStyles({
        errorBox: {
            backgroundColor: "#f2f2f2",
            display: 'flex',
            justifyContent: 'center',
            flexFlow: 'column',
            alignItems: 'center',
            height: '100vh',
            width: '100%'
        }
    })
);

const PageNotFound = () => {
    const classes = useStyles();

    return (
        <div className={classes.errorBox}>
            <img src={Error} alt="404-page-not-found" />
        </div>
    );
};

export default PageNotFound;