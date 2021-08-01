import React from 'react';
import Loader from "react-loader-spinner";
import { SpinnerProps } from './Spinner.types';
import 'react-loader-spinner/dist/loader/css/react-spinner-loader.css';
import styles from './Spinner.module.scss';

/**
 * Loading Spinner
 */
const Spinner = ({
    isFullPage = false,
    spinnerColor = "#ccc",
    spinnerWidth = 100,
    spinnerHeight = 100,
    spinnerType = "Puff",
    backgroundColor = "transparent"
}: SpinnerProps): JSX.Element => {
    const SpinContainer = (
        <div className={isFullPage ? styles.spinnerInnerContainer : styles.spinnerContainer} style={{ backgroundColor: backgroundColor }}>
            <Loader
                type={spinnerType}
                color={spinnerColor}
                height={spinnerHeight}
                width={spinnerWidth}
            />
        </div>
    );

    if (isFullPage) {
        return (
            <div className={styles.spinnerWrapper} style={{ backgroundColor: backgroundColor }}>
                {SpinContainer}
            </div>
        )
    }

    return SpinContainer;
};

export default Spinner;
