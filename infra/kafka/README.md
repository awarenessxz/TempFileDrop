# Apache Kafka

Using Apache Kafka for event streaming.

## Overview

Zookeeper is listening on port **2181** for the kafka service. -- Exposed on port **22181 & 32181** for host applications to 
access.

Kafka Service is advertised on port **9092** within the container environment -- Exposed on port **29092 & 39092** for host 
applications to access


## Usage

1. Start the instances: 
    - `sudo docker-compose up -d`

2. Execute commands through the shell
    - `sudo docker exec -it kafka-1 bash`

## Useful commands

### Topics

```bash
# Create Topic
kafka-topics --create --if-not-exists --zookeeper zookeeper-1:2181 --partitions 3 --replication-factor 1 --topic $TOPIC_NAME

# List Topics
kafka-topics --zookeeper zookeeper-1:2181 --list

# Get Topic Details
kafka-topics --zookeeper zookeeper-1:2181 --topic $TOPIC_NAME --describe

# Delete Topic
kafka-topics --zookeeper zookeeper-1:2181 --delete --topic $TOPIC_NAME
```

### Produce Message

```bash
kafka-console-producer --broker-list localhost:9092 --topic $TOPIC_NAME
```

### Consume Message

```bash
kafka-console-consumer --bootstrap-server localhost:9092 --topic $TOPIC_NAME --from-beginning
```

## References

- [Baeldung - Guide to setting up Apache Kafka using Docker](https://www.baeldung.com/ops/kafka-docker-setup)
- [Apache Kafka ClI Cheatsheet](https://medium.com/@TimvanBaarsen/apache-kafka-cli-commands-cheat-sheet-a6f06eac01b)
- [Hack to create topics on startup](https://github.com/confluentinc/examples/blob/5.1.1-post/microservices-orders/docker-compose.yml#L182-L215)
