// This data will be inserted into database given by 'MONGO_INITDB_DATABASE' environment variable.
// If the environment variable is not set then it will be inserted into database name 'test'

// Create an user with userAdminAnyDatabase role which grants
// the privilege to create other users on any existing database.
db.createUser({
    user: "admin",
    pwd: "1234",
    roles: [{ role: "userAdminAnyDatabase", db: "admin" }]
})

db.createUser({
    user: "service_user",
    pwd: "service_pass",
    roles: [{ role: "readWrite", db: "webDB" }]
});
db.auth("service_user", "service_pass")

// Collections for WebServer
db.createCollection("users_uploads");
db.createCollection("users");
db.users.insert([
    { username: "user1", password: "password", creationDate: new Date(Date.now()) },
    { username: "user2", password: "password", creationDate: new Date(Date.now()) }
]);

// Collections for Storage Service
db.createCollection("storage_info");