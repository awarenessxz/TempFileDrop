import React from "react";
import { makeStyles, createStyles } from '@material-ui/core/styles';
import Breadcrumbs from '@material-ui/core/Breadcrumbs';
import Link from '@material-ui/core/Link';

interface BreadcrumbsProps {
    path: string;
    callback: (path: string) => void;
}

const useStyles = makeStyles(() =>
    createStyles({
        link: {
            cursor: 'pointer',
            '&:hover': {
                color: 'green',
                textDecoration: 'underline',
            },
        },
        notLink: {
            cursor: 'normal',
            '&:hover': {
                textDecoration: 'none',
            },
        },
    }),
);


const BreadcrumbsBar = (props: BreadcrumbsProps) => {
    const classes = useStyles();

    const renderBreadcrumbs = () => {
        let linkPath = "";
        return props.path.split("/").map(label => {
            const path = linkPath.concat('/').concat(label);
            linkPath = path;
            const effects = `/${props.path}` === path
                ? { className: classes.notLink }
                : { onClick : () => props.callback(path), className: classes.link };
            return (
                <Link key={path} color="inherit" {...effects}>
                    {label}
                </Link>
            );
        });
    };

    return (
        <Breadcrumbs aria-label="breadcrumb">
            {renderBreadcrumbs()}
        </Breadcrumbs>
    );
};

export default BreadcrumbsBar;
