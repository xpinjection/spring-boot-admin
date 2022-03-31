# spring-boot-admin
Spring Boot Admin configured for K8S and local usage.

## Configuration

There are 2 configuration profiles: **local** and **k8s**.

Local is used for development needs when Spring Boot application is run at the same machine as Spring Boot Admin. Port and actuator endpoint for the local monitored application could be overridden with _monitored.application.port_ and _monitored.application.actuator-endpoint_ configuration properties (by default they point to _8080_ and _/admin_). In local mode Spring Boot Admin monitors itself as well. 
