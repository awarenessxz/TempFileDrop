import React, { MouseEvent, useState } from 'react';
import { Button } from "@material-ui/core";
import { TestComponentProps } from './TestComponent.types';
import styles from './TestComponent.module.scss';

/**
 * Test Component
 */
const TestComponent = ({
    theme = "primary",
    apiUrl = "/api/test/data"
}: TestComponentProps): JSX.Element => {
    const [apiMsg, setApiMsg] = useState("");
    const mThemeStyle = theme === 'primary' ? '' : styles.testComponentSecondary;

    const onButtonClick = (e: MouseEvent<HTMLButtonElement>) => {
        fetch(apiUrl)
            .then((res) => res.text())
            .then((data) => setApiMsg(data))
            .catch((err) => setApiMsg(err));
    };

    return (
        <div data-testid="test-component" className={`${styles.testComponent} ${mThemeStyle}`}>
            <h1 className={`${styles.heading}`}>I&apos;m the test component</h1>
            <h2>Made with love by Harvey</h2>
            <Button data-testid="test-button" variant="contained" color="primary" onClick={onButtonClick}>Call API</Button>
            {apiMsg && <p>{apiMsg}</p>}
        </div>
    );
};

export default TestComponent;
