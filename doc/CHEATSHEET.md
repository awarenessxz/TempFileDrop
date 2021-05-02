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

## Docker

```bash
docker ps - a                   # check list of containers instance
docker rm <CONTAINER_NAME/ID>   # delete container instance
docker rmi <IMAGE_NAME/ID>      # delete image
```
