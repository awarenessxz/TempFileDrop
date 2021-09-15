import React from "react";
import { makeStyles, Theme, createStyles } from '@material-ui/core/styles';
import Modal from '@material-ui/core/Modal';

interface VerticallyCenteredModalProps {
    title: string;
    content: React.ReactNode;
    width: number;
    show: boolean;
    onClose: () => void;
}

const useStyles = makeStyles((theme: Theme) =>
    createStyles({
        modal: {
            position: 'absolute',
            backgroundColor: theme.palette.background.paper,
            border: '2px solid #000',
            boxShadow: theme.shadows[5],
            padding: theme.spacing(2, 4, 3),
            top: '50%',
            left: '50%',
            transform: 'translate(-50%, -50%)',
        },
    }),
);

const VerticallyCenteredModal = (props: VerticallyCenteredModalProps) => {
    const classes = useStyles();

    return (
        <Modal open={props.show} onClose={props.onClose}>
            <div style={{ width: props.width }} className={classes.modal}>
                <h2>{props.title}</h2>
                {props.content}
            </div>
        </Modal>
    );
};

export default VerticallyCenteredModal;
