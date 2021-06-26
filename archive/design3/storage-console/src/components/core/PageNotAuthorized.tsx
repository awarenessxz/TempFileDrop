import React from 'react';
import { createStyles, makeStyles } from '@material-ui/core/styles';
import Error from "../../assets/unauthorized.png";

const useStyles = makeStyles(() =>
    createStyles({
        errorBox: {
            backgroundColor: "#fd9f3a",
            display: 'flex',
            justifyContent: 'center',
            flexFlow: 'column',
            alignItems: 'center',
            height: '100vh',
            width: '100%'
        }
    })
);

const PageNotAuthorized = () => {
    const classes = useStyles();

    return (
        <div className={classes.errorBox}>
            <img src={Error} alt="404-page-not-found" />
        </div>
    );
};

export default PageNotAuthorized;