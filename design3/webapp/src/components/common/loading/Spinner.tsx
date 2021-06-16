import React from "react";
import Loader from "react-loader-spinner";
import "./Spinner.css";
import "react-loader-spinner/dist/loader/css/react-spinner-loader.css";

type ReactLoaderSpinnerTypes = "Audio"
    |"BallTriangle"
    |"Bars"
    |"Circles"
    |"Grid"
    |"Hearts"
    |"Oval"
    |"Puff"
    |"Rings"
    |"TailSpin"
    |"ThreeDots"
    |"Watch"
    |"RevolvingDot"
    |"Triangle"
    |"Plane"
    |"MutatingDots"
    |"CradleLoader";

interface SpinnerProps {
    isFullPage?: boolean;
    spinnerColor?: string;
    spinnerWidth?: number;
    spinnerHeight?: number;
    spinnerType?: ReactLoaderSpinnerTypes;
    backgroundColor?: string;
}

const Spinner = ({
    isFullPage = false,
    spinnerColor = "#ccc",
    spinnerWidth = 100,
    spinnerHeight = 100,
    spinnerType = "Puff",
    backgroundColor = "transparent"
}: SpinnerProps) => {
    const SpinContainer = (
        <div className={`${isFullPage ? "spinner-inner-container" : "spinner-container"}`} style={{ backgroundColor: backgroundColor }}>
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
            <div className="spinner-wrapper" style={{ backgroundColor: backgroundColor }}>
                {SpinContainer}
            </div>
        )
    }

    return SpinContainer;
};

export default Spinner;