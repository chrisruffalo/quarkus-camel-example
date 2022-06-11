# Quarkus Camel Example

## This Example Includes...
- Yaml-based routes
- Direct Routes
- Normal Bean
- CDI Bean
- Annotation-based consumer
- Rest endpoint -> JMS messages

## Supporting Containers
When running in dev mode the following containers are required:

### Artemis QPID
```bash
 []$ docker run -it --rm -p 8161:8161 -p 61616:61616 -p 5672:5672 -e AMQ_USER=quarkus -e AMQ_PASSWORD=quarkus quay.io/artemiscloud/activemq-artemis-broker:0.1.4
```
