import React from "react";
import Loader from "react-loader-spinner";
import "./Spinner.css";
import "react-loader-spinner/dist/loader/css/react-spinner-loader.css";

const Spinner = () => {
    return (
        <div className="spinner-wrapper">
            <div className="spinner-container">
                <Loader
                    type="Puff"
                    color="#88D498"
                    height={400}
                    width={400}
                />
            </div>
        </div>
    )
};

export default Spinner;