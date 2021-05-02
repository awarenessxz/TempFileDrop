# Mongo Database 

Using Mongo to store application data.

## Usage

1. Clear data inside `mongodata` folder **[OPTIONAL]**
    - If you are starting a fresh copy of mongo db, make sure you delete everything except `.gitkeep` inside `mongodata` 
    folder. **Reason for doing so:** We are using `mongodata` folder as a mount folder to persist the data inside our 
    mongodb. Hence, if the folder have stuff, the init scripts inside `mongo` folder will not run.
        - `cd mongodata`
        - `rm -rf *`

2. Run container in background
    - `docker-compose up -d`
    - `docker ps -a` -- check if container is created
    - `docker logs <CONTAINER_NAME>` -- check logs of running instance

3. Access the container
    - `docker exec -it <CONTAINER_NAME> bash`
    - Access Mongo
        - `mongo -u root -p 1234`

4. Shut down Container
    - `docker-compose down -v`
    - `docker volume ls` -- check that volume is taken down as well
        
## Other useful commands

### Mongo

- Show databases -- `show dbs`
- Create database -- `use [DATABASE_NAME]`
- Check what database you are in -- `db`
- Delete database
- Create user
    ```
    db.createUser({
        user: "admin",
        pwd: "1234",
        roles: ["readWrite", "dbAdmin"]
    });
    ```
- Create Collection -- `db.createCollection('[NAME_OF_COLLECTION]`);
- List all Collections -- `show collections`
- List items in Collection -- `db.COLLECTION_NAME.find();`

### Docker

- Stop the container -- `docker stop [CONTAINER_ID / CONTAINER_NAME]`
- Remove the container -- `docker rm [CONTAINER_ID / CONTAINER_NAME]`
- Remove the image -- `docker rmi [IMAGE_ID / IMAGE_NAME]`
- Mongo
    - Pull Docker Image -- `docker pull mongo`
    - Run Container Instance -- `docker run -it -d -p 27017:27017 -v ~/path/to/java-web-app/database/mongodata:/data/db --name mongodb mongo`
    - Access Container -- `docker exec -it <CONTAINER_NAME> bash`
        - Access Mongo
            - `mongo` --> launch mongo
            - `use exampleDB` --> set the database
            - `show collections` --> list all the tables created
## References
- [Managing MongoDB on docker with docker-compose](https://medium.com/faun/managing-mongodb-on-docker-with-docker-compose-26bf8a0bbae3)
- [Write Mongo Scripts](https://docs.mongodb.com/manual/tutorial/write-scripts-for-the-mongo-shell/)
- [[VIDEO] configuring authentication in Mongo](https://www.youtube.com/watch?v=SY_9zwb29LA)
