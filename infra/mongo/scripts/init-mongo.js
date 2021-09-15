// This data will be inserted into database given by 'MONGO_INITDB_DATABASE' environment variable.
// If the environment variable is not set then it will be inserted into database name 'test'

/********************************************************************************
 * Create system wide admin account
 *******************************************************************************/

// Create an user with userAdminAnyDatabase role which grants
// the privilege to create other users on any existing database.
db.createUser({
    user: "admin",
    pwd: "1234",
    roles: [{ role: "userAdminAnyDatabase", db: "admin" }]
});

/********************************************************************************
 * Create Users
 *******************************************************************************/

db.auth('admin', '1234');
db.createUser({
    user: "service_user",
    pwd: "service_pass",
    roles: [{ role: "readWrite", db: "webDB" }]
});
db.createUser({
    user: "storage_user",
    pwd: "storage_pass",
    roles: [{ role: "readWrite", db: "storageDB" }]
});

/********************************************************************************
 * Create Database for TempFileDrop WebServer
 *******************************************************************************/

// authenticate user
db = db.getSiblingDB('admin');
db.auth("service_user", "service_pass");

// Create Collections for TempFileDrop webserver
db = db.getSiblingDB('webDB');
db.createCollection("users_upload_info");

// for design 1 only
db.createCollection("users");
db.users.insert([
    { username: "user1", password: "password", creationDate: new Date(Date.now()) },
    { username: "user2", password: "password", creationDate: new Date(Date.now()) }
]);

/********************************************************************************
 * Create Database for Storage Service
 *******************************************************************************/

// authenticate user
db = db.getSiblingDB('admin');
db.auth("storage_user", "storage_pass");

// Create Collections for Storage Service
db = db.getSiblingDB('storageDB');
// design 1-3 only
db.createCollection("storage_files");
db.createCollection("download_tokens");
db.createCollection("storage_info");
// all other designs
db.createCollection("storage_metadata");
db.createCollection("data_events");
db.createCollection("scheduled_jobs");
db.createCollection("watchlist");
