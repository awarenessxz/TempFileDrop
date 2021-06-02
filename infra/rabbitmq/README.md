# Rabbit MQ Cluster

RabbitMQ is set up to enable microservices to communicate via "Events". 

## Usage

1. Clear data inside `storage` folder **[OPTIONAL]**
    - If you are starting a fresh copy of rabbitmq, make sure you delete everything except `.gitkeep` inside `storage` 
    folder. **Reason for doing so:** We are using `storage` folder as a mount folder to persist the data inside our 
    rabbitmq.
        - `cd rabbitmq`
        - `rm -rf *`

1. Run container in background
    - `docker-compose up -d`
    - `docker ps -a` -- check if container is created
    - `docker logs rabbitmq` -- check logs of running instance

2. Access Rabbit MQ
    - browse to `http://localhost:15672`
    - login with user: `admin` and password `123`
    
3. Shut down Container
    - `docker-compose down -v`
    - `docker volume ls` -- check that volume is taken down as well

## Password

Generate password hash - `python scripts/password_hash.py`

| accounts | password |
| --- | --- |
| storage_user | storage123 |
| admin | admin123 |

## References
- [Spring Cloud Stream + Rabbit MQ](https://stackabuse.com/spring-cloud-stream-with-rabbitmq-message-driven-microservices/)
- [Intro to Cloud Stream](https://www.baeldung.com/spring-cloud-stream)
- [Spring Cloud Stream RabbitMQ Binder Reference Guide](https://cloud.spring.io/spring-cloud-static/spring-cloud-stream-binder-rabbit/2.2.0.M1/spring-cloud-stream-binder-rabbit.html)