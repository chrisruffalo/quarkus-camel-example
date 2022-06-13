package io.github.chrisruffalo.camel.example.config;

import io.quarkus.runtime.annotations.RegisterForReflection;
import org.apache.camel.component.amqp.AMQPConnectionDetails;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * This class is provided to sync configuration between jms/connection components,
 * AMQP components, and anything else that relies on `amqp-host`,etc that are set
 * in the quarkus application.properties (and that might change at deployment time)
 */
@RegisterForReflection
public class JmsConnectionConfiguration {

    @Inject
    Logger logger;

    @ConfigProperty(name = "amqp-protocol", defaultValue = "amqp")
    String amqpProtocol;

    @ConfigProperty(name = "amqp-host", defaultValue = "localhost")
    String amqpHost;

    @ConfigProperty(name = "amqp-port", defaultValue = "5672")
    Integer amqpPort;

    @ConfigProperty(name = "amqp-user", defaultValue = "admin")
    String amqpUser;

    @ConfigProperty(name = "amqp-password", defaultValue = "admin")
    String amqpPassword;

    /**
     * Produces the connection details, based on environment configuration, that is used
     * by `amqp` route components.
     *
     * @return
     */
    @ApplicationScoped
    @Named("amqpConnectionDetails")
    public AMQPConnectionDetails connectionDetails() {
        final String amqpBrokerUrl = String.format("%s://%s:%s", amqpProtocol, amqpHost, amqpPort);
        logger.infof("using broker url: %s", amqpBrokerUrl);
        return new AMQPConnectionDetails(amqpBrokerUrl, amqpUser, amqpPassword);
    }

}
