# Quarkus Camel Example

## Requirements
- Maven 3.8.x
- Docker

## This Example Includes...
- Yaml-based routes
- Direct Routes
- Normal Bean
- CDI Bean
- Annotation-based consumer
- Rest endpoint -> JMS messages
- AMQP Client Connections

## Configuration

#### AMQP Connection
- `amqp-host` (default: localhost) the amqp broker host
- `amqp-port` (default: 5762) the amqp broker port
- `amqp-user` (default: admin) the amqp broker user
- `amqp-password` (default: admin) the amqp broker password

## Running in Dev Mode
```bash
[]$ mvn quarkus:dev
```

#### Testing in Dev Mode
Use the Quarkus dev mode tests. Tests tagged with "full" are not run in dev mode because they
need injection/alternatives or something else that is not supported in dev mode.

## Running Locally in Jar/Native mode
When running outside of dev mode the following containers are required:
#### Artemis QPID
```bash
 []$ docker run -it --rm -p 8161:8161 -p 61616:61616 -p 5672:5672 -e AMQ_USER=admin -e AMQ_PASSWORD=admin quay.io/artemiscloud/activemq-artemis-broker:0.1.2
```
#### Testing in Jar/Native Mode
```bash
[]$ curl -v -H "Content-type: application/json" -d "here is some data" -X POST http://localhost:8080/submission 
```