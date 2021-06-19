import React, { MouseEvent } from "react";
import { useHistory } from "react-router-dom";
import { createStyles, makeStyles, Theme } from "@material-ui/core/styles";
import AppBar from "@material-ui/core/AppBar";
import Button from "@material-ui/core/Button";
import Toolbar from "@material-ui/core/Toolbar";
import IconButton from "@material-ui/core/IconButton";
import Typography from "@material-ui/core/Typography";
import MenuIcon from '@material-ui/icons/Menu';
import ChevronLeftIcon from '@material-ui/icons/ChevronLeft';
import { useAuthState } from "../util/auth-context";
import Data from "../config/app.json";

interface NavBarProps {
    isOpen: boolean;
    setIsOpen: (newState: boolean) => void;
}

const useStyles = makeStyles((theme: Theme) =>
    createStyles({
        appBar: {
            zIndex: theme.zIndex.drawer + 1,
        },
        title: {
            flexGrow: 1,
        },
    }),
);

const NavBar = (props: NavBarProps) => {
    const classes = useStyles();
    const history = useHistory();
    const { userToken, keycloak } = useAuthState();

    const handleLogout = (e: MouseEvent) => {
        e.preventDefault();
        history.push("/");
        keycloak?.logout();
    };

    return (
        <AppBar position="fixed" className={classes.appBar}>
            <Toolbar>
                <IconButton
                    color="inherit"
                    aria-label="open drawer"
                    onClick={() => props.setIsOpen(!props.isOpen)}
                    edge="start">
                    {props.isOpen ? <ChevronLeftIcon /> : <MenuIcon />}
                </IconButton>
                <Typography variant="h6" noWrap className={classes.title}>
                    {Data.app_name}
                </Typography>
                {userToken && (
                    <React.Fragment>
                        <Button color="inherit">{userToken.username}</Button>
                        <Button color="inherit" onClick={handleLogout}>Logout</Button>
                    </React.Fragment>
                )}
            </Toolbar>
        </AppBar>
    );
};

export default NavBar;
