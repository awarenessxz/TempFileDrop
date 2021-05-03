import React from 'react';
import Error from "../../assets/error.jpg";

const PageNotFound = () => {
    return (
        <div className="full-page">
            <img src={Error} alt="404-page-not-found" />
        </div>
    );
};

export default PageNotFound;