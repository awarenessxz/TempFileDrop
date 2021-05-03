import React from "react";
import Button from "react-bootstrap/cjs/Button";
import Modal, { ModalProps } from "react-bootstrap/cjs/Modal";

interface VerticallyCenteredModalProps extends ModalProps {
    title: string;
    content: React.ReactNode;
    show: boolean;
    onHide: () => void;
}

const VerticallyCenteredModal = (props: VerticallyCenteredModalProps): JSX.Element => {
    return (
        <Modal show={props.show} onHide={props.onHide} size="lg" aria-labelledby="contained-modal-title-vcenter" centered animation={false}>
            <Modal.Header closeButton>
                <Modal.Title id="contained-modal-title-vcenter">
                    { props.title }
                </Modal.Title>
            </Modal.Header>
            <Modal.Body>
                { props.content }
            </Modal.Body>
            <Modal.Footer>
                <Button onClick={props.onHide}>Close</Button>
            </Modal.Footer>
        </Modal>
    );
};

export default VerticallyCenteredModal;