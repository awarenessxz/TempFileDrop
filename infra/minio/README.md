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

## Configuring Event Notification

Download mc client

```bash
# Configure Localhost Minio Cluser
mc alias ls
mc alias set local http://localhost:9000 minio minio123
mc alias ls local

# Configure Notification
mc admin config get local | grep notify
mc admin config get local/ notify_amqp
mc admin config set local/ notify_amqp:1 exchange="minioBucketEvents" exchange_type="fanout" mandatory="off" no_wait="off" url="amqp://minio_admin:minio123@rabbitmq:5672" auto_deleted="off" delivery_mode="0" durable="on" internal="off" routing_key="bucketlogs"
mc admin service restart local/

# Create Bucket
mc mb local/<BUCKET_NAME>

# Configure Event to Bucket
mc event add local/<BUCKET_NAME> arn:minio:sqs::1:amqp
mc event list local/<BUCKET_NAME>
```

Note: rabbitmq refers to host name of docker image since localhost doesn't work.