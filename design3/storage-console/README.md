# Storage Console

Storage Console is a simple console for consumers/administrators to view their storage as well as trace the events.

![console view](../../doc/console.png)

## Getting Started

Ensure that the following services are running...
- Centralized Storage Service
- Storage Gateway
- RabbitMQ
- Keycloak
- MongoDB
- Minio Cluster

Start the web application

```bash
yarn install
yarn start
```

## Data Source

| Data | Source | Remarks |
| --- | --- | --- |
| events | data_events (DB) | list of events tagged to consumer |
