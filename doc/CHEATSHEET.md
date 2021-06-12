# Command Cheat Sheet

## Mongo

```bash
# Launch Mongo Interface
mongo -u <USER> -p <PASS>

# Login
use admin
db.auth('<USER>', '<PASS>');

# Database related
show dbs                # list all database
use <DATABASE_NAME>     # select database
db                      # Check what database you are in

# User related
db.createUser({ user: '<USER>', pwd: '<PASS>', roles: ['readWrite', 'dbAdmin'] });

# Collections related
db.createCollection('<COLLECTION_NAME>');   # create collection
show collections                            # list all collections
db.<COLLECTION_NAME>.find();                # List items in Collection
```

## Curl

```bash
curl -X POST -d '{"username": "user1", "password": "password" }' -H "Content-Type: application/json" http://localhost:7001/login

curl -H "Access-Control-Request-Method: GET" -H "Origin: http://localhost:3000" --head localhost:8001/api-docs
```

## Docker

```bash
docker ps - a                   # check list of containers instance
docker rm <CONTAINER_NAME/ID>   # delete container instance
docker rmi <IMAGE_NAME/ID>      # delete image
```

## Kubernetes 

```bash

```