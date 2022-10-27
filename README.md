# Spring Boot Admin

![CI status](https://github.com/xpinjection/spring-boot-admin/actions/workflows/maven.yml/badge.svg)

Spring Boot Admin configured for K8S and local usage.

## Configuration profiles

There are 2 configuration profiles: **local** and **k8s**. Profile has to be set on startup as a regular Spring profile. 

### Local mode

Local mode is used for development needs when Spring Boot application is run at the same machine as Spring Boot Admin. Port and actuator endpoint for the local monitored application could be overridden with _monitored.application.port_ and _monitored.application.actuator-endpoint_ configuration properties (by default they point to _8080_ and _/admin_). In local mode Spring Boot Admin monitors itself as well.

### K8S mode

K8S mode is used for deployment in K8S environment. Spring Boot services with Actuator enabled are discovered by the label _tech-stack: springboot_ set at the Service level. Discovery is performed by default in the same namespace where Spring Boot Admin is deployed. To enable discovery via K8S API service account must be configured to have _get, list, watch_ permissions on _pods, services, endpoints_ in the corresponding namespace.
