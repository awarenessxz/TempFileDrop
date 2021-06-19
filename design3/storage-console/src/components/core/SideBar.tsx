import React from "react";
import clsx from "clsx";
import { createStyles, makeStyles, Theme } from "@material-ui/core/styles";
import Drawer from '@material-ui/core/Drawer';
import List from '@material-ui/core/List';
import Toolbar from "@material-ui/core/Toolbar";
import ServiceIcon from '@material-ui/icons/AccountTree';
import DashboardIcon from '@material-ui/icons/Dashboard';
import StorageIcon from "@material-ui/icons/Unarchive";
import MuiLinkListItem from "../../util/MuiLinkListItem";

interface SideBarProps {
    isOpen: boolean;
    setIsOpen: (newState: boolean) => void;
}

const drawerWidth = 240;
const useStyles = makeStyles((theme: Theme) =>
    createStyles({
        drawer: {
            width: drawerWidth,
            flexShrink: 0,
            whiteSpace: 'nowrap',
        },
        drawerOpen: {
            width: drawerWidth,
            transition: theme.transitions.create('width', {
                easing: theme.transitions.easing.sharp,
                duration: theme.transitions.duration.enteringScreen,
            }),
        },
        drawerClose: {
            transition: theme.transitions.create('width', {
                easing: theme.transitions.easing.sharp,
                duration: theme.transitions.duration.leavingScreen,
            }),
            overflowX: 'hidden',
            width: theme.spacing(7) + 1,
            [theme.breakpoints.up('sm')]: {
                width: theme.spacing(9) + 1,
            },
        },
    }),
);

const SideBar = (props: SideBarProps) => {
    const classes = useStyles();

    return (
        <Drawer
            variant="permanent"
            className={clsx(classes.drawer, {
                [classes.drawerOpen]: props.isOpen,
                [classes.drawerClose]: !props.isOpen,
            })}
            classes={{
                paper: clsx({
                    [classes.drawerOpen]: props.isOpen,
                    [classes.drawerClose]: !props.isOpen,
                }),
            }}
        >
            <Toolbar />
            <List>
                <MuiLinkListItem primary="Dashboard" to="/" icon={<DashboardIcon />} />
                <MuiLinkListItem primary="Buckets" to="/buckets" icon={<StorageIcon />} />
                <MuiLinkListItem primary="Services" to="/services" icon={<ServiceIcon />} />
            </List>
        </Drawer>
    )
};

export default SideBar;
