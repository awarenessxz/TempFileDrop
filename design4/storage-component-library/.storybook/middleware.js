/**
 * A way to mock api calls in storybook
 * https://dev.to/mattc/how-to-mock-api-calls-in-storybook-1762
 */
const express = require('express');
const bodyParser = require('body-parser');

const testComponentUtil = require("../setup/mock-api-data/testcomponent-util");

const expressMiddleWare = router => {
    router.use(bodyParser.urlencoded({ extended: false }));
    router.use(bodyParser.json());

    /* ********************************************
     * API for Test Component
     ***********************************************/

    router.get('/api/test/data', (request, response) => {
        response.status(200).send(testComponentUtil.hello);
    });

    /* ********************************************
     * API for FileDropzone Component
     ***********************************************/

    router.post('/api/file-upload', (request, response) => {
        response.status(200).send("Success");
    });

    /* ********************************************
     * API for StorageFileDropzone Component
     ***********************************************/

    router.post('/api/storagesvc/anonymous/upload', (request, response) => {
        response.status(200).send({
            message: "Upload Success",
            storageId: "example-storage-id",
        });
    });

    // api for test component
    router.post('/api/storagesvc/upload', (request, response) => {
        response.status(200).send({
            message: "Upload Success",
            storageId: "example-storage-id",
        });
    });
};

module.exports = expressMiddleWare;