# MinIO Cluster

Using MinIO to store objects. Using the `docker-compose.yaml` file provided by MinIO. We will generate 4 MinIO server 
instances using Nginx as a reverse proxy and load balancer. 

## Usage

1. Purge all data in `storage` folder. **[OPTIONAL]**
    - Folders insides the `storage` folder are the persistent mounts used for each minio servers.
    - To clean the data, execute the command --> `sudo rm -rf storage`

2. Run the MinIO Distributed Cluster
    - `docker-compose up -d`
    - `docker ps -a` -- check if containers are created
    - `docker logs <CONTAINER_NAME>` -- check logs of containers

3. Access the server
    - `docker exec -it <CONTAINER_NAME> bash`   -- containers itself
    - Browse to `http://127.0.0.1:9000/`        -- MinIO admin console

4. Shut down Cluster
    - `docker-compose down -v`
    - `docker volume ls` -- check that volumes are taken down as well
