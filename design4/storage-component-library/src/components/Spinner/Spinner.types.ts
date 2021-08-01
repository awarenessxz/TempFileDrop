export type ReactLoaderSpinnerTypes = "Audio"
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

export interface SpinnerProps {
    /** Whether to cover entire screen or not */
    isFullPage?: boolean;
    /** Spinner Colour */
    spinnerColor?: string;
    /** Width of Spinner */
    spinnerWidth?: number;
    /** Height of Spinner */
    spinnerHeight?: number;
    /** Spinner Type */
    spinnerType?: ReactLoaderSpinnerTypes;
    /** Hex Color Code for Background (optional) */
    backgroundColor?: string;
}
