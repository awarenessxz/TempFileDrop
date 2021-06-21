# Archive Designs

## Design v1 - Backend Proxy

![design 1](../doc/architecture_design1.png)

This is the first conceptualize design of the centralized storage service. The design was built around the idea of using 
the application's backend webserver as the main mode of communication between the application and the centralized storage
service. Hence, any storage related requests will be routed (or proxied) from the application's frontend to the centralized
storage service. 

In this design, note that no authentication was implemented. For more information, Check out the [design 1 readme](design1)
and [centralized storage service documentation for design 1](design1/storage-service).

## Design v2 - Direct Consumption with Event Feedback

![design 1](../doc/architecture_design2.png) 

The second design is an upgrade to the first implementation. Instead of proxying the storage request, all storage request 
will be made directly to the centralized storage service. This is to reduce the amount of hops required to upload / download
files. 

Additionally, the following features were added to the design:
- anonymous upload / download
- authentication with keycloak
- event feedback with rabbitmq to notify when files upload/download/delete is completed

Check out the [design 2 readme](design2) and [centralized storage service documentation for design 2](design2/storage-service) 
for more details...

## Roles Authorization

Archived an implementation of storage service to document how to parse JWT Token to authorize keycloak roles using spring security.
